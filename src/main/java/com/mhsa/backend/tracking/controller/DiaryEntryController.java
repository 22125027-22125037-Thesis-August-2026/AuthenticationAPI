package com.mhsa.backend.tracking.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhsa.backend.common.util.SecurityUtils;
import com.mhsa.backend.tracking.dto.DiaryEntryRequest;
import com.mhsa.backend.tracking.dto.DiaryEntryResponse;
import com.mhsa.backend.tracking.service.DiaryEntryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/tracking/diaries")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Diary API")
public class DiaryEntryController {

    private final DiaryEntryService diaryEntryService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new diary entry")
    public ResponseEntity<DiaryEntryResponse> create(
            @RequestPart("diary") String diaryJson,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> files
    ) {
        int attachmentCount = files == null ? 0 : files.size();
        log.info("Create diary request received with {} attachment(s)", attachmentCount);

        DiaryEntryRequest request = parseAndValidateDiaryRequest(diaryJson, false);
        UUID profileId = SecurityUtils.getCurrentProfileId();
        log.debug("Submitting create diary request for profileId={}", profileId);

        DiaryEntryResponse response = diaryEntryService.create(profileId, request, files);
        log.info("Diary entry created successfully for profileId={}", profileId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{profileId}")
    @PreAuthorize("@accessGuard.canReadTrackingData(authentication, #profileId)")
    @Operation(summary = "Get all diary entries")
    public ResponseEntity<List<DiaryEntryResponse>> getAll(@PathVariable UUID profileId) {
        return ResponseEntity.ok(diaryEntryService.getAllByProfileId(profileId));
    }

    @GetMapping("/{profileId}/{id}")
    @PreAuthorize("@accessGuard.canReadTrackingData(authentication, #profileId)")
    @Operation(summary = "Get a diary entry by ID")
    public ResponseEntity<DiaryEntryResponse> getById(@PathVariable UUID profileId, @PathVariable UUID id) {
        return ResponseEntity.ok(diaryEntryService.getById(profileId, id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a diary entry by ID")
    public ResponseEntity<DiaryEntryResponse> update(
            @PathVariable UUID id,
            @RequestPart(value = "diary", required = false) String diaryJson,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> files
    ) {
        DiaryEntryRequest request = parseAndValidateDiaryRequest(diaryJson, true);
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(diaryEntryService.update(profileId, id, request, files));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a diary entry by ID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        diaryEntryService.delete(profileId, id);
        return ResponseEntity.noContent().build();
    }

    private DiaryEntryRequest parseAndValidateDiaryRequest(String diaryJson, boolean allowEmpty) {
        if (diaryJson == null || diaryJson.isBlank()) {
            if (!allowEmpty) {
                throw new RuntimeException("Missing required 'diary' payload");
            }
            return null;
        }

        DiaryEntryRequest request;
        try {
            request = objectMapper.readValue(diaryJson, DiaryEntryRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid diary JSON payload", e);
        }

        Set<ConstraintViolation<DiaryEntryRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return request;
    }
}
