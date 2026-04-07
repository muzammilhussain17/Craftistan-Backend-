package com.craftistan.admin.service;

import com.craftistan.admin.dto.*;
import com.craftistan.notification.service.EmailService;
import com.craftistan.order.repository.OrderRepository;
import com.craftistan.product.entity.ApprovalStatus;
import com.craftistan.product.entity.Product;
import com.craftistan.product.repository.ProductRepository;
import com.craftistan.report.entity.Report;
import com.craftistan.report.entity.ReportStatus;
import com.craftistan.report.repository.ReportRepository;
import com.craftistan.review.repository.ReviewRepository;
import com.craftistan.user.entity.AccountStatus;
import com.craftistan.user.entity.Role;
import com.craftistan.user.entity.User;
import com.craftistan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final EmailService emailService;

    // ==============================
    // Dashboard Stats
    // ==============================

    public AdminStatsDto getDashboardStats() {
        long totalBuyers = userRepository.countByRole(Role.BUYER);
        long totalArtisans = userRepository.countByRole(Role.ARTISAN);
        long pendingArtisans = userRepository.countByRoleAndIsVerifiedFalse(Role.ARTISAN);
        long totalProducts = productRepository.count();
        long pendingProducts = productRepository.countByApprovalStatus(ApprovalStatus.PENDING);
        long approvedProducts = productRepository.countByApprovalStatus(ApprovalStatus.APPROVED);
        long rejectedProducts = productRepository.countByApprovalStatus(ApprovalStatus.REJECTED);
        long totalOrders = orderRepository.count();
        long openReports = reportRepository.countByStatus(ReportStatus.OPEN);

        return AdminStatsDto.builder()
                .totalBuyers(totalBuyers)
                .totalArtisans(totalArtisans)
                .pendingArtisanVerifications(pendingArtisans)
                .totalProducts(totalProducts)
                .pendingProducts(pendingProducts)
                .approvedProducts(approvedProducts)
                .rejectedProducts(rejectedProducts)
                .totalOrders(totalOrders)
                .openReports(openReports)
                .build();
    }

    // ==============================
    // User Management
    // ==============================

    public Page<User> getAllUsers(Role role, AccountStatus status, Pageable pageable) {
        if (role != null && status != null) {
            return userRepository.findByRoleAndAccountStatus(role, status, pageable);
        } else if (role != null) {
            return userRepository.findByRole(role, pageable);
        } else if (status != null) {
            return userRepository.findByAccountStatus(status, pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    @Transactional
    public User updateUserStatus(String id, AccountStatus newStatus) {
        User user = getUserById(id);
        user.setAccountStatus(newStatus);
        // Disable login for suspended/banned users
        user.setEnabled(newStatus == AccountStatus.ACTIVE);
        log.info("Admin updated user {} status to {}", id, newStatus);
        return userRepository.save(user);
    }

    // ==============================
    // Artisan Verification
    // ==============================

    public Page<User> getPendingArtisans(Pageable pageable) {
        return userRepository.findByRoleAndIsVerifiedFalse(Role.ARTISAN, pageable);
    }

    @Transactional
    public User verifyArtisan(String id, boolean approved, String notes) {
        User artisan = getUserById(id);
        if (artisan.getRole() != Role.ARTISAN) {
            throw new RuntimeException("User is not an artisan");
        }
        artisan.setIsVerified(approved);
        log.info("Admin {} artisan {}: {}", approved ? "approved" : "rejected", id, notes);
        User saved = userRepository.save(artisan);

        // Send verification result email
        if (approved) {
            emailService.sendArtisanVerifiedEmail(saved.getEmail(), saved.getName());
        } else {
            emailService.sendArtisanRejectedEmail(saved.getEmail(), saved.getName(), notes);
        }

        return saved;
    }

    // ==============================
    // Product Moderation
    // ==============================

    public Page<Product> getProductsByStatus(ApprovalStatus status, Pageable pageable) {
        if (status != null) {
            return productRepository.findByApprovalStatus(status, pageable);
        }
        return productRepository.findAll(pageable);
    }

    @Transactional
    public Product approveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        product.setApprovalStatus(ApprovalStatus.APPROVED);
        product.setAdminNotes(null);
        log.info("Admin approved product {}", productId);
        return productRepository.save(product);
    }

    @Transactional
    public Product rejectProduct(Long productId, String notes) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        product.setApprovalStatus(ApprovalStatus.REJECTED);
        product.setAdminNotes(notes);
        log.info("Admin rejected product {}: {}", productId, notes);
        return productRepository.save(product);
    }

    @Transactional
    public Product toggleFeatured(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        product.setIsFeatured(!product.getIsFeatured());
        log.info("Admin toggled featured for product {} -> {}", productId, product.getIsFeatured());
        return productRepository.save(product);
    }

    // ==============================
    // Review Moderation
    // ==============================

    @Transactional
    public void hideReview(Long reviewId) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found: " + reviewId));
        review.setIsHidden(true);
        reviewRepository.save(review);
        log.info("Admin hid review {}", reviewId);
    }

    @Transactional
    public void flagReview(Long reviewId) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found: " + reviewId));
        review.setIsFlagged(true);
        reviewRepository.save(review);
        log.info("Admin flagged review {}", reviewId);
    }

    // ==============================
    // Reports & Disputes
    // ==============================

    public Page<Report> getReports(ReportStatus status, Pageable pageable) {
        if (status != null) {
            return reportRepository.findByStatus(status, pageable);
        }
        return reportRepository.findAll(pageable);
    }

    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));
    }

    @Transactional
    public Report updateReportStatus(Long id, ReportStatus newStatus, String resolutionNote) {
        Report report = getReportById(id);
        report.setStatus(newStatus);
        if (resolutionNote != null) {
            report.setResolutionNote(resolutionNote);
        }
        if (newStatus == ReportStatus.RESOLVED) {
            report.setResolvedAt(LocalDateTime.now());
        }
        log.info("Admin updated report {} to {}", id, newStatus);
        Report saved = reportRepository.save(report);

        // Email the reporter when the report is actioned
        if (newStatus == ReportStatus.RESOLVED || newStatus == ReportStatus.DISMISSED) {
            userRepository.findById(saved.getReporterId()).ifPresent(reporter ->
                    emailService.sendReportResolvedEmail(
                            reporter.getEmail(), reporter.getName(),
                            newStatus.name(), resolutionNote));
        }

        return saved;
    }
}
