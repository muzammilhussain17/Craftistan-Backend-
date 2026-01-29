package com.craftistan.order.service;

import com.craftistan.common.exception.ResourceNotFoundException;
import com.craftistan.order.dto.CreateOrderRequest;
import com.craftistan.order.dto.OrderDto;
import com.craftistan.order.entity.Order;
import com.craftistan.order.entity.OrderItem;
import com.craftistan.order.entity.OrderStatus;
import com.craftistan.order.repository.OrderRepository;
import com.craftistan.product.entity.Product;
import com.craftistan.product.repository.ProductRepository;
import com.craftistan.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("5000");
    private static final BigDecimal SHIPPING_COST = new BigDecimal("200");

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request, User user) {
        // Generate order ID
        String orderId = generateOrderId();

        // Calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        Order order = Order.builder()
                .id(orderId)
                .userId(user.getId())
                .paymentMethod(request.getPaymentMethod())
                .shippingFullName(request.getShippingAddress().getFullName())
                .shippingPhone(request.getShippingAddress().getPhone())
                .shippingAddress(request.getShippingAddress().getAddress())
                .shippingCity(request.getShippingAddress().getCity())
                .shippingPostalCode(request.getShippingAddress().getPostalCode())
                .build();

        // Add items
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));

            OrderItem item = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getImage())
                    .price(product.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .artisanId(product.getArtisanId())
                    .build();

            order.addItem(item);
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        // Calculate shipping
        BigDecimal shippingCost = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : SHIPPING_COST;

        order.setSubtotal(subtotal);
        order.setShippingCost(shippingCost);
        order.setTotal(subtotal.add(shippingCost));

        Order saved = orderRepository.save(order);
        return toDto(saved);
    }

    public Page<OrderDto> getUserOrders(String userId, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByUserIdAndStatus(userId, status, pageable).map(this::toDto);
        }
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toDto);
    }

    public OrderDto getOrderById(String orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Check ownership
        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return toDto(order);
    }

    @Transactional
    public OrderDto cancelOrder(String orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
            throw new RuntimeException("Cannot cancel order in " + order.getStatus() + " status");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        return toDto(saved);
    }

    // Artisan methods
    public Page<OrderDto> getArtisanOrders(String artisanId, Pageable pageable) {
        return orderRepository.findOrdersByArtisan(artisanId, pageable).map(this::toDto);
    }

    @Transactional
    public OrderDto updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(status);
        Order saved = orderRepository.save(order);
        return toDto(saved);
    }

    private String generateOrderId() {
        String prefix = "ORD-" + LocalDate.now().getYear() + "-";
        long count = orderRepository.countOrdersWithPrefix(prefix);
        return String.format("%s%04d", prefix, count + 1);
    }

    private OrderDto toDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .shippingCost(order.getShippingCost())
                .total(order.getTotal())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddress(OrderDto.ShippingAddressDto.builder()
                        .fullName(order.getShippingFullName())
                        .phone(order.getShippingPhone())
                        .address(order.getShippingAddress())
                        .city(order.getShippingCity())
                        .postalCode(order.getShippingPostalCode())
                        .build())
                .items(order.getItems().stream()
                        .map(item -> OrderDto.OrderItemDto.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .productImage(item.getProductImage())
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
