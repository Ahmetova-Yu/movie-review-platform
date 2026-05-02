package com.moviereview.reviewservice.repository;

import com.moviereview.reviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByFilmId(Long filmId);
    List<Review> findByUserId(Long userId);
    Optional<Review> findByFilmIdAndUserId(Long filmId, Long userId);

    @Modifying
    @Query("UPDATE Review r SET r.likesCount = r.likesCount + 1 WHERE r.id = :reviewId")
    void incrementLikes(Long reviewId);

    List<Review> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}