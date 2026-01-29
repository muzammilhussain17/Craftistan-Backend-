package com.craftistan.user.controller;

import com.craftistan.auth.dto.AuthResponse;
import com.craftistan.common.dto.ApiResponse;
import com.craftistan.upload.service.FileUploadService;
import com.craftistan.user.dto.ChangePasswordRequest;
import com.craftistan.user.dto.UpdateProfileRequest;
import com.craftistan.user.entity.User;
import com.craftistan.user.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management")
public class ProfileController {

    private final ProfileService profileService;
    private final FileUploadService fileUploadService;

    @GetMapping
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<AuthResponse.UserDto>> getProfile(
            @AuthenticationPrincipal User user) {
        AuthResponse.UserDto profile = profileService.getProfile(user);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping
    @Operation(summary = "Update profile")
    public ResponseEntity<ApiResponse<AuthResponse.UserDto>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User user) {
        AuthResponse.UserDto profile = profileService.updateProfile(request, user);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile updated successfully"));
    }

    @PutMapping("/password")
    @Operation(summary = "Change password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User user) {
        profileService.changePassword(request, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @PostMapping("/avatar")
    @Operation(summary = "Upload avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws IOException {
        String avatarUrl = fileUploadService.uploadFile(file);
        profileService.updateAvatar(avatarUrl, user);
        return ResponseEntity.ok(ApiResponse.success(avatarUrl, "Avatar updated successfully"));
    }
}
