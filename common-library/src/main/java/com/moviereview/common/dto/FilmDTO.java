package com.moviereview.common.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class FilmDTO {
    private Long id;
    private String title;
    private String originalTitle;
    private String description;
    private Integer releaseYear;
    private Double averageRating;
    private Set<String> genres;
    private String posterUrl;
}