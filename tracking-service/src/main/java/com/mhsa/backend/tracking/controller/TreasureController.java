package com.mhsa.backend.tracking.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhsa.backend.auth.jwt.SecurityUtils;
import com.mhsa.backend.tracking.dto.TreasureRequest;
import com.mhsa.backend.tracking.dto.TreasureResponse;
import com.mhsa.backend.tracking.service.TreasureService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/tracking/treasures")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Treasure API")
public class TreasureController {

    private final TreasureService treasureService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a treasure, optionally with a single media item")
    public ResponseEntity<TreasureResponse> create(
            @RequestPart("treasure") String treasureJson,
            @RequestPart(value = "media", required = false) MultipartFile media
    ) {
        boolean hasMedia = media != null && !media.isEmpty();
        log.info("Create treasure request received, hasMedia={}", hasMedia);

        TreasureRequest request = parseAndValidate(treasureJson);
        UUID profileId = SecurityUtils.getCurrentProfileId();

        TreasureResponse response = treasureService.create(profileId, request, media);
        log.info("Treasure created successfully for profileId={}", profileId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/")
    @Operation(summary = "Get the current user's treasures, newest first")
    public ResponseEntity<List<TreasureResponse>> getAll() {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(treasureService.getAllByProfileId(profileId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a treasure (and its media) by ID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        treasureService.delete(profileId, id);
        return ResponseEntity.noContent().build();
    }

    private TreasureRequest parseAndValidate(String treasureJson) {
        if (treasureJson == null || treasureJson.isBlank()) {
            throw new RuntimeException("Missing required 'treasure' payload");
        }

        TreasureRequest request;
        try {
            request = objectMapper.readValue(treasureJson, TreasureRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid treasure JSON payload", e);
        }

        Set<ConstraintViolation<TreasureRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return request;
    }
}
