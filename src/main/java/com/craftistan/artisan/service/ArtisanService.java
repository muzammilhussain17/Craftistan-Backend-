package com.craftistan.artisan.service;

import com.craftistan.artisan.dto.ArtisanDashboardDto;
import com.craftistan.order.repository.OrderRepository;
import com.craftistan.product.repository.ProductRepository;
import com.craftistan.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ArtisanService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ArtisanDashboardDto getDashboard(User artisan) {
        long totalProducts = productRepository.findByArtisanId(artisan.getId()).size();
        long totalOrders = orderRepository.countOrdersByArtisan(artisan.getId());

        // Calculate average rating from products
        double avgRating = productRepository.findByArtisanId(artisan.getId())
                .stream()
                .mapToDouble(p -> p.getRating() != null ? p.getRating() : 0)
                .average()
                .orElse(0.0);

        return ArtisanDashboardDto.builder()
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalRevenue(BigDecimal.ZERO) // TODO: Calculate from order items
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .pendingOrders(0) // TODO: Calculate
                .completedOrders(0) // TODO: Calculate
                .build();
    }
}
