package com.mhsa.backend.tracking.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.auth.jwt.SecurityUtils;
import com.mhsa.backend.tracking.dto.StepLogRequest;
import com.mhsa.backend.tracking.dto.StepLogResponse;
import com.mhsa.backend.tracking.service.StepLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/steps")
@RequiredArgsConstructor
@Tag(name = "Steps API")
public class StepLogController {

    private final StepLogService stepLogService;

    @PostMapping("/")
    @Operation(summary = "Create or update today's step log")
    public ResponseEntity<StepLogResponse> upsert(@Valid @RequestBody StepLogRequest request) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.status(HttpStatus.CREATED).body(stepLogService.upsert(profileId, request));
    }

    @GetMapping("/{profileId}")
    @PreAuthorize("@accessGuard.canReadTrackingData(authentication, #profileId)")
    @Operation(summary = "Get all step logs for a profile")
    public ResponseEntity<List<StepLogResponse>> getAll(@PathVariable UUID profileId) {
        return ResponseEntity.ok(stepLogService.getAllByProfileId(profileId));
    }

    @GetMapping("/{profileId}/range")
    @PreAuthorize("@accessGuard.canReadTrackingData(authentication, #profileId)")
    @Operation(summary = "Get step logs for a profile within a date range")
    public ResponseEntity<List<StepLogResponse>> getByDateRange(
            @PathVariable UUID profileId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(stepLogService.getByDateRange(profileId, from, to));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a step log by ID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        stepLogService.delete(profileId, id);
        return ResponseEntity.noContent().build();
    }
}
