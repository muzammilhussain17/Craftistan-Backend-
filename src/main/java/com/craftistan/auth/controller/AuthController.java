package com.craftistan.auth.controller;

import com.craftistan.auth.dto.AuthResponse;
import com.craftistan.auth.dto.LoginRequest;
import com.craftistan.auth.dto.RegisterRequest;
import com.craftistan.auth.service.AuthService;
import com.craftistan.common.dto.ApiResponse;
import com.craftistan.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user and get JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(
                    AuthResponse.builder()
                            .success(false)
                            .message("Not authenticated")
                            .build());
        }
        return ResponseEntity.ok(authService.getCurrentUser(user.getEmail()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OTP Password Reset
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset OTP")
    public ResponseEntity<ApiResponse<AuthResponse>> forgotPassword(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(ApiResponse.success(authService.forgotPassword(request.get("email"))));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify the 6-digit OTP")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.verifyOtp(request.get("email"), request.get("otp"))));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using verified OTP")
    public ResponseEntity<ApiResponse<AuthResponse>> resetPassword(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.resetPassword(request.get("email"), request.get("otp"), request.get("newPassword"))));
    }
}
