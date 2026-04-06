package com.craftistan.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDto {
    private long totalBuyers;
    private long totalArtisans;
    private long pendingArtisanVerifications;
    private long totalProducts;
    private long pendingProducts;
    private long approvedProducts;
    private long rejectedProducts;
    private long totalOrders;
    private long openReports;
}
