package com.moviereview.filmservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "films")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String title;

    private String originalTitle;

    @Column(length = 5000)
    private String description;

    private Integer releaseYear;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer totalRatings = 0;

    @ElementCollection
    @CollectionTable(name = "film_genres", joinColumns = @JoinColumn(name = "film_id"))
    @Column(name = "genre")
    @Builder.Default
    private Set<String> genres = new HashSet<>();

    private String posterUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}