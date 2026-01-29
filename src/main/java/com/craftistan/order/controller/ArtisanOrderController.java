package com.craftistan.order.controller;

import com.craftistan.common.dto.ApiResponse;
import com.craftistan.order.dto.OrderDto;
import com.craftistan.order.entity.OrderStatus;
import com.craftistan.order.service.OrderService;
import com.craftistan.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artisan/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ARTISAN')")
@Tag(name = "Artisan Orders", description = "Order management for artisans")
public class ArtisanOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get orders containing artisan's products")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getArtisanOrders(
            @AuthenticationPrincipal User artisan,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<OrderDto> orders = orderService.getArtisanOrders(artisan.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        OrderDto order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success(order, "Order status updated"));
    }
}
