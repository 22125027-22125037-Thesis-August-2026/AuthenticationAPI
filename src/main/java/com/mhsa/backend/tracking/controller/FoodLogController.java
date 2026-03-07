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
import com.mhsa.backend.tracking.dto.FoodLogRequest;
import com.mhsa.backend.tracking.dto.FoodLogResponse;
import com.mhsa.backend.tracking.service.FoodLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/foods")
@RequiredArgsConstructor
@Tag(name = "Food API")
public class FoodLogController {

    private final FoodLogService foodLogService;

    @PostMapping("/")
    @Operation(summary = "Create a new food log")
    public ResponseEntity<FoodLogResponse> create(@Valid @RequestBody FoodLogRequest request) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.status(HttpStatus.CREATED).body(foodLogService.create(profileId, request));
    }

    @GetMapping("/")
    @Operation(summary = "Get all food logs")
    public ResponseEntity<List<FoodLogResponse>> getAll() {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(foodLogService.getAllByProfile(profileId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a food log by ID")
    public ResponseEntity<FoodLogResponse> getById(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(foodLogService.getById(profileId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a food log by ID")
    public ResponseEntity<FoodLogResponse> update(@PathVariable UUID id, @Valid @RequestBody FoodLogRequest request) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(foodLogService.update(profileId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a food log by ID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        foodLogService.delete(profileId, id);
        return ResponseEntity.noContent().build();
    }
}
