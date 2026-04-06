package com.craftistan.admin.controller;

import com.craftistan.admin.dto.AdminStatsDto;
import com.craftistan.admin.service.AdminService;
import com.craftistan.common.dto.ApiResponse;
import com.craftistan.product.entity.ApprovalStatus;
import com.craftistan.report.entity.ReportStatus;
import com.craftistan.user.entity.AccountStatus;
import com.craftistan.user.entity.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Super Admin management endpoints")
public class AdminController {

    private final AdminService adminService;

    // ==============================
    // Dashboard
    // ==============================

    @GetMapping("/stats")
    @Operation(summary = "Get platform dashboard stats")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
    }

    // ==============================
    // User Management
    // ==============================

    @GetMapping("/users")
    @Operation(summary = "List all users with optional filters")
    public ResponseEntity<ApiResponse<?>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) AccountStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllUsers(role, status, pageable)));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get a single user's details")
    public ResponseEntity<ApiResponse<?>> getUser(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserById(id)));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "Update user account status (ACTIVE, SUSPENDED, BANNED)")
    public ResponseEntity<ApiResponse<?>> updateUserStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        AccountStatus newStatus = AccountStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(adminService.updateUserStatus(id, newStatus)));
    }

    // ==============================
    // Artisan Verification
    // ==============================

    @GetMapping("/artisans/pending")
    @Operation(summary = "List artisans awaiting verification")
    public ResponseEntity<ApiResponse<?>> getPendingArtisans(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingArtisans(pageable)));
    }

    @PutMapping("/artisans/{id}/verify")
    @Operation(summary = "Approve or reject an artisan application")
    public ResponseEntity<ApiResponse<?>> verifyArtisan(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        boolean approved = (Boolean) body.getOrDefault("approved", false);
        String notes = (String) body.getOrDefault("notes", "");
        return ResponseEntity.ok(ApiResponse.success(adminService.verifyArtisan(id, approved, notes)));
    }

    // ==============================
    // Product Moderation
    // ==============================

    @GetMapping("/products")
    @Operation(summary = "List all products with optional status filter")
    public ResponseEntity<ApiResponse<?>> getProducts(
            @RequestParam(required = false) ApprovalStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getProductsByStatus(status, pageable)));
    }

    @PutMapping("/products/{id}/approve")
    @Operation(summary = "Approve a product — it becomes publicly visible")
    public ResponseEntity<ApiResponse<?>> approveProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.approveProduct(id)));
    }

    @PutMapping("/products/{id}/reject")
    @Operation(summary = "Reject a product with an optional reason note")
    public ResponseEntity<ApiResponse<?>> rejectProduct(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String notes = body.getOrDefault("notes", "");
        return ResponseEntity.ok(ApiResponse.success(adminService.rejectProduct(id, notes)));
    }

    @PutMapping("/products/{id}/feature")
    @Operation(summary = "Toggle featured status of a product")
    public ResponseEntity<ApiResponse<?>> toggleFeatured(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.toggleFeatured(id)));
    }

    // ==============================
    // Review Moderation
    // ==============================

    @DeleteMapping("/reviews/{id}")
    @Operation(summary = "Hide/remove a review")
    public ResponseEntity<ApiResponse<Void>> hideReview(@PathVariable Long id) {
        adminService.hideReview(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/reviews/{id}/flag")
    @Operation(summary = "Flag a review for moderator attention")
    public ResponseEntity<ApiResponse<Void>> flagReview(@PathVariable Long id) {
        adminService.flagReview(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ==============================
    // Reports & Disputes
    // ==============================

    @GetMapping("/reports")
    @Operation(summary = "List all reports with optional status filter")
    public ResponseEntity<ApiResponse<?>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getReports(status, pageable)));
    }

    @GetMapping("/reports/{id}")
    @Operation(summary = "Get full report details")
    public ResponseEntity<ApiResponse<?>> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getReportById(id)));
    }

    @PutMapping("/reports/{id}/status")
    @Operation(summary = "Update a report status and optionally add resolution note")
    public ResponseEntity<ApiResponse<?>> updateReportStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        ReportStatus newStatus = ReportStatus.valueOf(body.get("status").toUpperCase());
        String note = body.get("resolutionNote");
        return ResponseEntity.ok(ApiResponse.success(adminService.updateReportStatus(id, newStatus, note)));
    }
}
