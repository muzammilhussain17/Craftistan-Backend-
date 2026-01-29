package com.craftistan.order.controller;

import com.craftistan.common.dto.ApiResponse;
import com.craftistan.order.dto.CreateOrderRequest;
import com.craftistan.order.dto.OrderDto;
import com.craftistan.order.entity.OrderStatus;
import com.craftistan.order.service.OrderService;
import com.craftistan.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal User user) {
        OrderDto order = orderService.createOrder(request, user);
        return ResponseEntity.ok(ApiResponse.success(order, "Order placed successfully"));
    }

    @GetMapping
    @Operation(summary = "Get user's orders")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<OrderDto> orders = orderService.getUserOrders(user.getId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        OrderDto order = orderService.getOrderById(id, user);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        OrderDto order = orderService.cancelOrder(id, user);
        return ResponseEntity.ok(ApiResponse.success(order, "Order cancelled successfully"));
    }
}
