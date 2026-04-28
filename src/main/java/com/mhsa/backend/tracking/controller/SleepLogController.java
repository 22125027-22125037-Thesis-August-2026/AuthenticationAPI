package com.mhsa.backend.tracking.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.common.util.SecurityUtils;
import com.mhsa.backend.tracking.dto.SleepLogRequest;
import com.mhsa.backend.tracking.dto.SleepLogResponse;
import com.mhsa.backend.tracking.service.SleepLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/sleeps")
@RequiredArgsConstructor
@Tag(name = "Sleep API")
public class SleepLogController {

    private final SleepLogService sleepLogService;

    @PostMapping("/")
    @Operation(summary = "Create a new sleep log")
    public ResponseEntity<SleepLogResponse> create(@Valid @RequestBody SleepLogRequest request) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.status(HttpStatus.CREATED).body(sleepLogService.create(profileId, request));
    }

    @GetMapping("/{profileId}")
    @PreAuthorize("@accessGuard.canReadTrackingData(authentication, #profileId)")
    @Operation(summary = "Get all sleep logs")
    public ResponseEntity<List<SleepLogResponse>> getAll(@PathVariable UUID profileId) {
        return ResponseEntity.ok(sleepLogService.getAllByProfileId(profileId));
    }

    @GetMapping("/{profileId}/{id}")
    @PreAuthorize("@accessGuard.canReadTrackingData(authentication, #profileId)")
    @Operation(summary = "Get a sleep log by ID")
    public ResponseEntity<SleepLogResponse> getById(@PathVariable UUID profileId, @PathVariable UUID id) {
        return ResponseEntity.ok(sleepLogService.getById(profileId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a sleep log by ID")
    public ResponseEntity<SleepLogResponse> update(@PathVariable UUID id, @Valid @RequestBody SleepLogRequest request) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(sleepLogService.update(profileId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sleep log by ID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        sleepLogService.delete(profileId, id);
        return ResponseEntity.noContent().build();
    }
}
