package com.craftistan.auth.service;

import com.craftistan.auth.dto.AuthResponse;
import com.craftistan.auth.dto.LoginRequest;
import com.craftistan.auth.dto.RegisterRequest;
import com.craftistan.config.JwtUtils;
import com.craftistan.notification.service.EmailService;
import com.craftistan.user.entity.Role;
import com.craftistan.user.entity.User;
import com.craftistan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import com.craftistan.user.repository.PasswordResetTokenRepository;
import com.craftistan.user.entity.PasswordResetToken;
import com.craftistan.common.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordResetTokenRepository tokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtils jwtUtils;
        private final AuthenticationManager authenticationManager;
        private final EmailService emailService;

        public AuthResponse register(RegisterRequest request) {
                try {
                        // Check if email already exists
                        if (userRepository.existsByEmail(request.getEmail())) {
                                return AuthResponse.builder()
                                                .success(false)
                                                .message("Email already registered")
                                                .build();
                        }

                        // Create new user
                        User user = User.builder()
                                        .name(request.getName())
                                        .email(request.getEmail())
                                        .password(passwordEncoder.encode(request.getPassword()))
                                        .role(request.getRole() != null ? request.getRole() : Role.BUYER)
                                        .build();

                        User savedUser = userRepository.save(user);

                        // Send welcome email asynchronously
                        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());

                        return AuthResponse.builder()
                                        .success(true)
                                        .message("Account created successfully for " + savedUser.getName()
                                                        + ". Please login.")
                                        .build();
                } catch (Exception e) {
                        return AuthResponse.builder()
                                        .success(false)
                                        .message("Registration failed: " + e.getMessage())
                                        .build();
                }
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Forgot Password Flow
        // ─────────────────────────────────────────────────────────────────────────

        @Transactional
        public AuthResponse forgotPassword(String email) {
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isEmpty()) {
                        return AuthResponse.builder()
                                        .success(false)
                                        .message("No account found with this email address")
                                        .build();
                }
                User user = userOpt.get();

                // Generate 6-digit OTP safely
                SecureRandom random = new SecureRandom();
                int num = random.nextInt(1000000);
                String otp = String.format("%06d", num);

                // Check for existing token and delete
                tokenRepository.findByEmail(email).ifPresent(tokenRepository::delete);

                // Create new token valid for 2 minutes
                PasswordResetToken resetToken = PasswordResetToken.builder()
                                .email(email)
                                .otp(otp)
                                .expiryDate(LocalDateTime.now().plusMinutes(2))
                                .build();
                
                tokenRepository.save(resetToken);

                // Trigger Email
                emailService.sendPasswordResetOtpEmail(user.getEmail(), user.getName(), otp);

                return AuthResponse.builder()
                                .success(true)
                                .message("OTP sent to your email successfully")
                                .build();
        }

        @Transactional
        public AuthResponse verifyOtp(String email, String otp) {
                PasswordResetToken token = tokenRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("No active OTP session found for this email"));

                if (token.isExpired()) {
                        tokenRepository.delete(token); // cleanup
                        return AuthResponse.builder().success(false).message("OTP has expired").build();
                }

                if (!token.getOtp().equals(otp)) {
                        return AuthResponse.builder().success(false).message("Invalid OTP").build();
                }

                return AuthResponse.builder().success(true).message("OTP verified successfully").build();
        }

        @Transactional
        public AuthResponse resetPassword(String email, String otp, String newPassword) {
                // Verify again just in case bypassing
                AuthResponse verify = verifyOtp(email, otp);
                if (!verify.isSuccess()) {
                        return verify;
                }

                // Update User
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
                
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);

                // Consume Token
                tokenRepository.deleteByEmail(email);

                return AuthResponse.builder()
                                .success(true)
                                .message("Password reset successfully. You can now log in.")
                                .build();
        }

        public AuthResponse login(LoginRequest request) {
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getEmail(),
                                                        request.getPassword()));
                } catch (BadCredentialsException e) {
                        return AuthResponse.builder()
                                        .success(false)
                                        .message("Invalid email or password")
                                        .build();
                }

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Map<String, Object> claims = new HashMap<>();
                claims.put("role", user.getRole().name());
                claims.put("name", user.getName());

                String token = jwtUtils.generateToken(user, claims);

                return AuthResponse.builder()
                                .success(true)
                                .accessToken(token)
                                .user(AuthResponse.UserDto.builder()
                                                .id(user.getId())
                                                .name(user.getName())
                                                .email(user.getEmail())
                                                .role(user.getRole())
                                                .avatar(user.getAvatar())
                                                .build())
                                .build();
        }

        public AuthResponse getCurrentUser(String email) {
                User user = userRepository.findByEmail(email)
                                .orElse(null);

                if (user == null) {
                        return AuthResponse.builder()
                                        .success(false)
                                        .message("User not found")
                                        .build();
                }

                return AuthResponse.builder()
                                .success(true)
                                .user(AuthResponse.UserDto.builder()
                                                .id(user.getId())
                                                .name(user.getName())
                                                .email(user.getEmail())
                                                .role(user.getRole())
                                                .avatar(user.getAvatar())
                                                .build())
                                .build();
        }
}
