package com.moviereview.reviewservice.service;

import com.moviereview.common.dto.ReviewDTO;
import com.moviereview.common.event.FilmRatedEvent;
import com.moviereview.reviewservice.entity.Review;
import com.moviereview.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Map<Long, AtomicLong> filmRatingSumCache = new ConcurrentHashMap<>();
    private final Map<Long, AtomicLong> filmRatingCountCache = new ConcurrentHashMap<>();

    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        log.info("Создание рецензии на фильм {} от пользователя {}", reviewDTO.getFilmId(), reviewDTO.getUserId());

        Optional<Review> existing = reviewRepository.findByFilmIdAndUserId(
                reviewDTO.getFilmId(), reviewDTO.getUserId());

        if (existing.isPresent()) {
            throw new RuntimeException("Вы уже оставляли рецензию на этот фильм");
        }

        Review review = Review.builder()
                .filmId(reviewDTO.getFilmId())
                .userId(reviewDTO.getUserId())
                .username(reviewDTO.getUsername())
                .rating(reviewDTO.getRating())
                .reviewText(reviewDTO.getReviewText())
                .build();

        Review saved = reviewRepository.save(review);

        updateFilmRatingAsync(saved.getFilmId());

        FilmRatedEvent event = FilmRatedEvent.builder()
                .filmId(saved.getFilmId())
                .userId(saved.getUserId())
                .newRating(saved.getRating())
                .username(saved.getUsername())
                .timestamp(LocalDateTime.now())
                .build();
        kafkaTemplate.send("film-rated", saved.getFilmId().toString(), event);

        return mapToDTO(saved);
    }

    @Async
    public CompletableFuture<Void> updateFilmRatingAsync(Long filmId) {
        return CompletableFuture.runAsync(() -> {
            log.debug("Асинхронное обновление рейтинга фильма: {}", filmId);

            List<Review> reviews = reviewRepository.findByFilmId(filmId);

            int sum = reviews.parallelStream().mapToInt(Review::getRating).sum();
            int count = reviews.size();
            double newAverage = count > 0 ? (double) sum / count : 0.0;

            filmRatingSumCache.computeIfAbsent(filmId, k -> new AtomicLong()).set(sum);
            filmRatingCountCache.computeIfAbsent(filmId, k -> new AtomicLong()).set(count);

            log.info("Рейтинг фильма {} обновлён: avg={} из {} оценок", filmId, newAverage, count);
        });
    }

    public List<ReviewDTO> getReviewsByFilm(Long filmId) {
        return reviewRepository.findByFilmId(filmId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ReviewDTO> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void likeReview(Long reviewId) {
        reviewRepository.incrementLikes(reviewId);
        log.info("Лайк добавлен рецензии: {}", reviewId);
    }

    public Map<String, Object> getDailyStats() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Review> todayReviews = reviewRepository.findByCreatedAtBetween(startOfDay, endOfDay);

        if (todayReviews.isEmpty()) {
            return Map.of("totalReviews", 0);
        }

        int totalReviews = todayReviews.size();
        int totalLikes = todayReviews.parallelStream().mapToInt(Review::getLikesCount).sum();
        double avgRating = todayReviews.parallelStream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Map<String, Long> topUsers = todayReviews.parallelStream()
                .collect(Collectors.groupingBy(Review::getUsername, Collectors.counting()));

        return Map.of(
                "totalReviews", totalReviews,
                "totalLikes", totalLikes,
                "averageRating", avgRating,
                "topUsers", topUsers
        );
    }

    private ReviewDTO mapToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .filmId(review.getFilmId())
                .userId(review.getUserId())
                .username(review.getUsername())
                .rating(review.getRating())
                .reviewText(review.getReviewText())
                .likesCount(review.getLikesCount())
                .createdAt(review.getCreatedAt())
                .build();
    }
}