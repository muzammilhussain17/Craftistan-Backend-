package com.craftistan.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.craftistan.notification.entity.Notification;
import com.craftistan.user.repository.UserRepository;
import com.craftistan.user.entity.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Value("${app.mail.from:muzammilhussain0a0@gmail.com}")
    private String fromEmail;

    private static final String FROM_NAME = "Craftistan";

    // ─────────────────────────────────────────────────────────────────────────
    // Core send method (async — never blocks a request thread)
    // ─────────────────────────────────────────────────────────────────────────

    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context ctx = new Context();
            ctx.setVariables(variables);
            String htmlContent = templateEngine.process("email/" + templateName, ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, FROM_NAME);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email '{}' sent to {}", subject, to);
            
            // Generate in-app notification
            createInAppNotificationFromEmail(to, subject, templateName);
        } catch (Exception e) {
            log.error("Failed to send email '{}' to {}: {}", subject, to, e.getMessage());
        }
    }

    private void createInAppNotificationFromEmail(String email, String title, String templateName) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String type = switch(templateName) {
                case "order-confirmation", "artisan-new-order", "order-status-update", "order-cancelled" -> "ORDER";
                case "new-review" -> "REVIEW";
                case "welcome", "artisan-verified", "artisan-rejected", "report-resolved" -> "SYSTEM";
                default -> "SYSTEM";
            };
            
            String message = switch(templateName) {
                case "order-confirmation" -> "Your order has been confirmed.";
                case "artisan-new-order" -> "You received a new order!";
                case "order-status-update" -> "Your order status was updated.";
                case "order-cancelled" -> "An order was cancelled.";
                case "new-review" -> "You received a new product review.";
                case "welcome" -> "Welcome to Craftistan!";
                case "artisan-verified" -> "Your artisan account is approved!";
                case "artisan-rejected" -> "Update regarding your artisan application.";
                case "report-resolved" -> "Your filed report was resolved.";
                default -> "You have a new notification.";
            };
            
            notificationService.createNotification(user.getId(), type, title, message, null);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Password Reset Email — triggered on forgot password
    // ─────────────────────────────────────────────────────────────────────────

    public void sendPasswordResetOtpEmail(String to, String name, String otp) {
        sendHtmlEmail(to, "Your Password Reset OTP — Craftistan", "password-reset", Map.of(
                "name", name,
                "otp", otp
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Welcome Email — triggered on registration
    // ─────────────────────────────────────────────────────────────────────────

    public void sendWelcomeEmail(String to, String name) {
        sendHtmlEmail(to, "Welcome to Craftistan 🎉", "welcome", Map.of(
                "name", name,
                "email", to
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Order Confirmation — triggered for buyer on order creation
    // ─────────────────────────────────────────────────────────────────────────

    public void sendOrderConfirmationEmail(String to, String name, String orderId,
                                           BigDecimal total, String paymentMethod,
                                           List<OrderItemData> items) {
        sendHtmlEmail(to, "Order Confirmed — " + orderId, "order-confirmation", Map.of(
                "name", name,
                "orderId", orderId,
                "total", total,
                "paymentMethod", paymentMethod,
                "items", items
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Artisan New Order Alert — triggered when buyer places order
    // ─────────────────────────────────────────────────────────────────────────

    public void sendArtisanNewOrderEmail(String to, String artisanName, String orderId,
                                         List<OrderItemData> items, BigDecimal total) {
        sendHtmlEmail(to, "🛒 New Order Received — " + orderId, "artisan-new-order", Map.of(
                "artisanName", artisanName,
                "orderId", orderId,
                "items", items,
                "total", total
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Order Status Update — triggered when artisan updates order status
    // ─────────────────────────────────────────────────────────────────────────

    public void sendOrderStatusUpdateEmail(String to, String name, String orderId, String status) {
        String subject = switch (status.toUpperCase()) {
            case "PROCESSING" -> "Your Order is Being Prepared — " + orderId;
            case "SHIPPED"    -> "📦 Your Order is On Its Way! — " + orderId;
            case "DELIVERED"  -> "✅ Order Delivered — " + orderId;
            default           -> "Order Update — " + orderId;
        };
        sendHtmlEmail(to, subject, "order-status-update", Map.of(
                "name", name,
                "orderId", orderId,
                "status", status
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Order Cancellation — triggered for buyer AND artisan
    // ─────────────────────────────────────────────────────────────────────────

    public void sendOrderCancelledEmail(String to, String name, String orderId, boolean isArtisan) {
        sendHtmlEmail(to, "Order Cancelled — " + orderId, "order-cancelled", Map.of(
                "name", name,
                "orderId", orderId,
                "isArtisan", isArtisan
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Artisan Verification — triggered after admin approves or rejects
    // ─────────────────────────────────────────────────────────────────────────

    public void sendArtisanVerifiedEmail(String to, String name) {
        sendHtmlEmail(to, "🎉 Congratulations! Your Artisan Account is Verified", "artisan-verified", Map.of(
                "name", name
        ));
    }

    public void sendArtisanRejectedEmail(String to, String name, String notes) {
        sendHtmlEmail(to, "Artisan Application Update", "artisan-rejected", Map.of(
                "name", name,
                "notes", notes != null ? notes : ""
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // New Review — triggered for artisan when a buyer reviews their product
    // ─────────────────────────────────────────────────────────────────────────

    public void sendNewReviewEmail(String to, String artisanName, String productName,
                                   String reviewerName, int rating, String comment) {
        sendHtmlEmail(to, "⭐ New Review on " + productName, "new-review", Map.of(
                "artisanName", artisanName,
                "productName", productName,
                "reviewerName", reviewerName,
                "rating", rating,
                "comment", comment != null ? comment : ""
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Report Resolved — triggered when admin updates a report status
    // ─────────────────────────────────────────────────────────────────────────

    public void sendReportResolvedEmail(String to, String name, String status, String resolutionNote) {
        sendHtmlEmail(to, "Your Report Has Been " + status, "report-resolved", Map.of(
                "name", name,
                "status", status,
                "resolutionNote", resolutionNote != null ? resolutionNote : ""
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner DTO for email templates
    // ─────────────────────────────────────────────────────────────────────────

    public record OrderItemData(String productName, int quantity, BigDecimal price) {}
}
