package com.mhsa.backend.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.common.dto.ApiResponse;
import com.mhsa.backend.common.util.SecurityUtils;
import com.mhsa.backend.dashboard.dto.DashboardSummaryDto;
import com.mhsa.backend.dashboard.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getSummary() {
        DashboardSummaryDto summary = dashboardService.getSummary(SecurityUtils.getCurrentProfileId());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
