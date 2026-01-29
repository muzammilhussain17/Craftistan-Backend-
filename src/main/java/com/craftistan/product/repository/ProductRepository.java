package com.craftistan.product.repository;

import com.craftistan.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find by artisan
    Page<Product> findByArtisanIdAndIsActiveTrue(String artisanId, Pageable pageable);

    List<Product> findByArtisanId(String artisanId);

    // Find by category
    Page<Product> findByCategoryAndIsActiveTrue(String category, Pageable pageable);

    // Search products
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

    // Filter products
    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (:style IS NULL OR p.style = :style) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findByFilters(
            @Param("category") String category,
            @Param("style") String style,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // Find active products
    Page<Product> findByIsActiveTrue(Pageable pageable);

    // Find new arrivals
    Page<Product> findByIsNewTrueAndIsActiveTrue(Pageable pageable);
}
