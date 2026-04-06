package com.craftistan.review.entity;

import com.craftistan.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "product_id", "user_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_avatar")
    private String userAvatar;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(length = 1000)
    private String comment;

    @Builder.Default
    private Integer helpful = 0;

    @Builder.Default
    private Boolean verified = false; // Verified purchase

    // Admin moderation
    @Builder.Default
    private Boolean isHidden = false; // Admin hid this review

    @Builder.Default
    private Boolean isFlagged = false; // Flagged for review by user/admin
}
