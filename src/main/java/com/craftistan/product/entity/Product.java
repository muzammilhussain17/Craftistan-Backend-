package com.craftistan.product.entity;

import com.craftistan.common.entity.BaseEntity;
import jakarta.persistence.*;
import com.craftistan.product.entity.ApprovalStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String category;

    private String style;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Builder.Default
    private Integer stock = 0;

    @Column(name = "artisan_id", nullable = false)
    private String artisanId;

    @Column(name = "artisan_name")
    private String artisanName;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "is_new")
    @Builder.Default
    private Boolean isNew = true;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Admin moderation — products start as PENDING, only APPROVED appear publicly
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    // Reason shown to artisan when their product is rejected
    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;

    // Featured on homepage showcase
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    // Language of original content (e.g., "en", "ur")
    @Column(name = "original_language")
    @Builder.Default
    private String originalLanguage = "en";

    // JSON storing translations: {"ur": {"name": "...", "description": "..."}, ...}
    @Column(name = "translations", columnDefinition = "TEXT")
    private String translations;

    // Helper method to get primary image
    public String getImage() {
        return images != null && !images.isEmpty() ? images.get(0) : null;
    }
}
