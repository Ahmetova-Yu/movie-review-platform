package com.moviereview.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmRatedEvent {
    private Long filmId;
    private Long userId;
    private Integer newRating;
    private String username;
    private LocalDateTime timestamp;
}