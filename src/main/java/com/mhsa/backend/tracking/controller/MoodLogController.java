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
import com.mhsa.backend.tracking.dto.MoodLogRequest;
import com.mhsa.backend.tracking.dto.MoodLogResponse;
import com.mhsa.backend.tracking.service.MoodLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/moods")
@RequiredArgsConstructor
@Tag(name = "Mood API")
public class MoodLogController {

    private final MoodLogService moodLogService;

    @PostMapping("/")
    @Operation(summary = "Create a new mood log")
    public ResponseEntity<MoodLogResponse> create(@Valid @RequestBody MoodLogRequest request) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.status(HttpStatus.CREATED).body(moodLogService.create(profileId, request));
    }

    @GetMapping("/")
    @Operation(summary = "Get all mood logs")
    public ResponseEntity<List<MoodLogResponse>> getAll() {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(moodLogService.getAllByProfileId(profileId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a mood log by ID")
    public ResponseEntity<MoodLogResponse> getById(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(moodLogService.getById(profileId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a mood log by ID")
    public ResponseEntity<MoodLogResponse> update(@PathVariable UUID id, @Valid @RequestBody MoodLogRequest request) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(moodLogService.update(profileId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a mood log by ID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        moodLogService.delete(profileId, id);
        return ResponseEntity.noContent().build();
    }
}
