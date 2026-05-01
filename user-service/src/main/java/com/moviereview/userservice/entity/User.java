package com.moviereview.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    private String role;

    @Builder.Default
    private Integer reputation = 0;

    @Builder.Default
    private Integer reviewsCount = 0;

    @Builder.Default
    private Integer likesReceived = 0;

    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;

    @ElementCollection
    @CollectionTable(name = "user_favorite_films", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "film_id")
    @Builder.Default
    private Set<Long> favoriteFilmIds = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastActiveAt = LocalDateTime.now();
    }
}