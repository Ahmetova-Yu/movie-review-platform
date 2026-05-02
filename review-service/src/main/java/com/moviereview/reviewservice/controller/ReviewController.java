package com.moviereview.reviewservice.controller;

import com.moviereview.common.dto.ReviewDTO;
import com.moviereview.reviewservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@RequestBody ReviewDTO reviewDTO) {
        log.info("POST /reviews - создание рецензии");
        return new ResponseEntity<>(reviewService.createReview(reviewDTO), HttpStatus.CREATED);
    }

    @GetMapping("/film/{filmId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByFilm(@PathVariable Long filmId) {
        log.info("GET /reviews/film/{} - получение рецензий фильма", filmId);
        return ResponseEntity.ok(reviewService.getReviewsByFilm(filmId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByUser(@PathVariable Long userId) {
        log.info("GET /reviews/user/{} - получение рецензий пользователя", userId);
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

    @PostMapping("/{reviewId}/like")
    public ResponseEntity<Void> likeReview(@PathVariable Long reviewId) {
        log.info("POST /reviews/{}/like - добавление лайка", reviewId);
        reviewService.likeReview(reviewId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<Map<String, Object>> getDailyStats() {
        log.info("GET /reviews/stats/daily - получение дневной статистики");
        return ResponseEntity.ok(reviewService.getDailyStats());
    }
}