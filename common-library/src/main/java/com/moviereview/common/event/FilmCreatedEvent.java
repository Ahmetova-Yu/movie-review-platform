package com.moviereview.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmCreatedEvent {
    private Long filmId;
    private String title;
    private Integer releaseYear;
}