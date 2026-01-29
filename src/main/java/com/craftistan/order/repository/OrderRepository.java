package com.craftistan.order.repository;

import com.craftistan.order.entity.Order;
import com.craftistan.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    // Find orders by user
    Page<Order> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Find orders by status
    Page<Order> findByUserIdAndStatus(String userId, OrderStatus status, Pageable pageable);

    // Find orders containing products from a specific artisan
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.artisanId = :artisanId ORDER BY o.createdAt DESC")
    Page<Order> findOrdersByArtisan(@Param("artisanId") String artisanId, Pageable pageable);

    // Count orders for artisan dashboard
    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items i WHERE i.artisanId = :artisanId")
    long countOrdersByArtisan(@Param("artisanId") String artisanId);

    // Generate next order ID
    @Query("SELECT COUNT(o) FROM Order o WHERE o.id LIKE :prefix%")
    long countOrdersWithPrefix(@Param("prefix") String prefix);
}
