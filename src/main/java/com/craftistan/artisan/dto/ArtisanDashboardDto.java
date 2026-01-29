package com.craftistan.artisan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtisanDashboardDto {

    private long totalProducts;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private double averageRating;
    private long pendingOrders;
    private long completedOrders;
}
