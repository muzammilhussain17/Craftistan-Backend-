package com.craftistan.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String category;
    private String style;
    private String image; // Primary image
    private List<String> images;
    private Integer stock;
    private String artisanId;
    private String artisanName;
    private Double rating;
    private Integer reviewCount;
    private Boolean isNew;
    private LocalDateTime createdAt;
}
