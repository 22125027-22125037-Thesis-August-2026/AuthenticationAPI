package com.mhsa.backend.dashboard.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mhsa.backend.contract.ApiResponse;
import com.mhsa.backend.dashboard.dto.DashboardSummaryResponse;
import com.mhsa.backend.dashboard.jwt.SecurityUtils;
import com.mhsa.backend.dashboard.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get aggregated dashboard summary for the current user.
     * Calls auth, tracking, and ai services in parallel and aggregates results.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(profileId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * Get user context for the specified number of days.
     * Retrieves tracking data (mood, sleep, diary, food) from tracking service.
     */
    @GetMapping("/context/{profileId}")
    @PreAuthorize("@accessGuard.canReadDashboard(authentication, #profileId)")
    public ResponseEntity<ApiResponse<JsonNode>> getUserContext(
            @PathVariable UUID profileId,
            @RequestParam(defaultValue = "7") int days) {
        JsonNode context = dashboardService.getUserContext(profileId, days);
        return ResponseEntity.ok(ApiResponse.success(context));
    }

    /**
     * Health check endpoint - verifies all downstream services are reachable.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<ObjectNode>> getServiceHealth() {
        ObjectNode health = dashboardService.getServiceHealth();
        return ResponseEntity.ok(ApiResponse.success(health));
    }
}
