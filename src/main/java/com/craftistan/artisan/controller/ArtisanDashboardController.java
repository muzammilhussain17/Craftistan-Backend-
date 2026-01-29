package com.craftistan.artisan.controller;

import com.craftistan.artisan.dto.ArtisanDashboardDto;
import com.craftistan.artisan.service.ArtisanService;
import com.craftistan.common.dto.ApiResponse;
import com.craftistan.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/artisan")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ARTISAN')")
@Tag(name = "Artisan Dashboard", description = "Artisan dashboard endpoints")
public class ArtisanDashboardController {

    private final ArtisanService artisanService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get artisan dashboard stats")
    public ResponseEntity<ApiResponse<ArtisanDashboardDto>> getDashboard(
            @AuthenticationPrincipal User artisan) {
        ArtisanDashboardDto dashboard = artisanService.getDashboard(artisan);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
