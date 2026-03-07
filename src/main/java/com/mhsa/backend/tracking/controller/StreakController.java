package com.mhsa.backend.tracking.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.common.util.SecurityUtils;
import com.mhsa.backend.tracking.dto.StreakRequest;
import com.mhsa.backend.tracking.dto.StreakResponse;
import com.mhsa.backend.tracking.service.StreakService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/streaks")
@RequiredArgsConstructor
@Tag(name = "Streak API")
public class StreakController {

    private final StreakService streakService;

    @PostMapping("/")
    @Operation(summary = "Create a new streak")
    public ResponseEntity<StreakResponse> create(@Valid @RequestBody StreakRequest request) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.status(HttpStatus.CREATED).body(streakService.create(profileId, request));
    }

    @GetMapping("/")
    @Operation(summary = "Get all streaks")
    public ResponseEntity<List<StreakResponse>> getAll() {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(streakService.getAllByProfileId(profileId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a streak by ID")
    public ResponseEntity<StreakResponse> getById(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(streakService.getById(profileId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a streak by ID")
    public ResponseEntity<StreakResponse> update(@PathVariable UUID id, @Valid @RequestBody StreakRequest request) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(streakService.update(profileId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a streak by ID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        streakService.delete(profileId, id);
        return ResponseEntity.noContent().build();
    }
}
