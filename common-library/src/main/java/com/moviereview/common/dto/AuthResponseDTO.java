package com.moviereview.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {
    private String token;
    private Long userId;
    private String username;
    private String role;
    private String tokenType;
}