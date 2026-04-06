package com.craftistan.report.controller;

import com.craftistan.common.dto.ApiResponse;
import com.craftistan.report.entity.Report;
import com.craftistan.report.entity.ReportStatus;
import com.craftistan.report.entity.ReportTargetType;
import com.craftistan.report.repository.ReportRepository;
import com.craftistan.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Dispute and report filing by buyers and artisans")
public class ReportController {

    private final ReportRepository reportRepository;

    @PostMapping
    @Operation(summary = "File a new report or dispute")
    public ResponseEntity<ApiResponse<Report>> fileReport(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User currentUser) {

        Report report = Report.builder()
                .reporterId(currentUser.getId())
                .reporterName(currentUser.getName())
                .targetId(body.get("targetId"))
                .targetType(ReportTargetType.valueOf(body.get("targetType").toUpperCase()))
                .reason(body.get("reason"))
                .description(body.getOrDefault("description", ""))
                .status(ReportStatus.OPEN)
                .build();

        Report saved = reportRepository.save(report);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's filed reports")
    public ResponseEntity<ApiResponse<?>> getMyReports(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 10) Pageable pageable) {
        var reports = reportRepository.findByReporterId(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }
}
