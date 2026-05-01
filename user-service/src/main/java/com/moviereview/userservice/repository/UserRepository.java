package com.moviereview.userservice.repository;

import com.moviereview.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.reputation = u.reputation + :delta WHERE u.id = :userId")
    void updateReputation(Long userId, Integer delta);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.reviewsCount = u.reviewsCount + 1 WHERE u.id = :userId")
    void incrementReviewsCount(Long userId);
}