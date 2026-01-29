package com.craftistan.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private Long id;
    private Long productId;
    private String userId;
    private String userName;
    private String userAvatar;
    private Integer rating;
    private String comment;
    private Integer helpful;
    private Boolean verified;
    private LocalDateTime createdAt;
}
