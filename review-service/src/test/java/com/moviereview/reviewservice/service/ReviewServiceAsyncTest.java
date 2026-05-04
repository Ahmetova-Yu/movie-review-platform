package com.moviereview.reviewservice.service;

import com.moviereview.reviewservice.entity.Review;
import com.moviereview.reviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class ReviewServiceAsyncTest {

    @Autowired
    private ReviewService reviewService;

    @MockBean
    private ReviewRepository reviewRepository;

    @Test
    void updateFilmRatingAsync_shouldCompleteSuccessfully() throws ExecutionException, InterruptedException {
        // given
        Review review1 = Review.builder().rating(10).build();
        Review review2 = Review.builder().rating(8).build();
        when(reviewRepository.findByFilmId(1L)).thenReturn(Arrays.asList(review1, review2));

        // when
        CompletableFuture<Void> future = reviewService.updateFilmRatingAsync(1L);
        future.join(); // дожидаемся завершения

        // then
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }
}