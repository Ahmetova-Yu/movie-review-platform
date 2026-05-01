package com.moviereview.common.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewDTO {
    private Long id;
    private Long filmId;
    private Long userId;
    private String username;
    private Integer rating;
    private String reviewText;
    private Integer likesCount;
    private LocalDateTime createdAt;
}