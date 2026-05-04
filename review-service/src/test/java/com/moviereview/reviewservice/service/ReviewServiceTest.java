package com.moviereview.reviewservice.service;

import com.moviereview.common.dto.ReviewDTO;
import com.moviereview.common.event.FilmRatedEvent;
import com.moviereview.reviewservice.entity.Review;
import com.moviereview.reviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ReviewService reviewService;

    private Review testReview;
    private ReviewDTO testReviewDTO;

    @BeforeEach
    void setUp() {
        testReview = Review.builder()
                .id(1L)
                .filmId(1L)
                .userId(1L)
                .username("anna")
                .rating(10)
                .reviewText("Excellent movie!")
                .likesCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        testReviewDTO = ReviewDTO.builder()
                .id(1L)
                .filmId(1L)
                .userId(1L)
                .username("anna")
                .rating(10)
                .reviewText("Excellent movie!")
                .build();
    }

    @Test
    void createReview_shouldSaveAndSendEvent() {
        // given
        when(reviewRepository.findByFilmIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // when
        ReviewDTO result = reviewService.createReview(testReviewDTO);

        // then
        assertNotNull(result);
        assertEquals(10, result.getRating());

        ArgumentCaptor<FilmRatedEvent> eventCaptor = ArgumentCaptor.forClass(FilmRatedEvent.class);
        verify(kafkaTemplate).send(eq("film-rated"), anyString(), eventCaptor.capture());

        FilmRatedEvent sentEvent = eventCaptor.getValue();
        assertEquals(1L, sentEvent.getFilmId());
        assertEquals(10, sentEvent.getNewRating());
    }

    @Test
    void createReview_whenAlreadyReviewed_shouldThrowException() {
        // given
        when(reviewRepository.findByFilmIdAndUserId(1L, 1L)).thenReturn(Optional.of(testReview));

        // when & then
        assertThrows(RuntimeException.class, () -> reviewService.createReview(testReviewDTO));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void getReviewsByFilm_shouldReturnList() {
        // given
        Review review2 = Review.builder()
                .id(2L)
                .filmId(1L)
                .userId(2L)
                .username("boris")
                .rating(8)
                .build();
        when(reviewRepository.findByFilmId(1L)).thenReturn(Arrays.asList(testReview, review2));

        // when
        List<ReviewDTO> result = reviewService.getReviewsByFilm(1L);

        // then
        assertEquals(2, result.size());
        assertEquals(10, result.get(0).getRating());
        assertEquals(8, result.get(1).getRating());
    }

    @Test
    void likeReview_shouldIncrementLikes() {
        // given
        doNothing().when(reviewRepository).incrementLikes(1L);

        // when
        reviewService.likeReview(1L);

        // then
        verify(reviewRepository).incrementLikes(1L);
    }

    @Test
    void getDailyStats_shouldCalculateCorrectStats() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Review review1 = Review.builder()
                .id(1L)
                .username("anna")
                .rating(10)
                .likesCount(5)
                .createdAt(now)
                .build();
        Review review2 = Review.builder()
                .id(2L)
                .username("boris")
                .rating(8)
                .likesCount(3)
                .createdAt(now)
                .build();

        when(reviewRepository.findByCreatedAtBetween(any(), any()))
                .thenReturn(Arrays.asList(review1, review2));

        // when
        Map<String, Object> stats = reviewService.getDailyStats();

        // then
        assertEquals(2, stats.get("totalReviews"));
        assertEquals(8, stats.get("totalLikes"));
        assertEquals(9.0, (Double) stats.get("averageRating"));

        @SuppressWarnings("unchecked")
        Map<String, Long> topUsers = (Map<String, Long>) stats.get("topUsers");
        assertEquals(1, topUsers.get("anna"));
        assertEquals(1, topUsers.get("boris"));
    }
}