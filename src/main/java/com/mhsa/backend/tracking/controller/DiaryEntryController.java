package com.mhsa.backend.tracking.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mhsa.backend.common.util.SecurityUtils;
import com.mhsa.backend.tracking.dto.DiaryEntryRequest;
import com.mhsa.backend.tracking.dto.DiaryEntryResponse;
import com.mhsa.backend.tracking.service.DiaryEntryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/diaries")
@RequiredArgsConstructor
@Tag(name = "Diary API")
public class DiaryEntryController {

    private final DiaryEntryService diaryEntryService;

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new diary entry")
    public ResponseEntity<DiaryEntryResponse> create(
            @RequestPart("diary") @Valid DiaryEntryRequest request,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> files
    ) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryEntryService.create(profileId, request, files));
    }

    @GetMapping("/")
    @Operation(summary = "Get all diary entries")
    public ResponseEntity<List<DiaryEntryResponse>> getAll() {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(diaryEntryService.getAllByProfileId(profileId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a diary entry by ID")
    public ResponseEntity<DiaryEntryResponse> getById(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(diaryEntryService.getById(profileId, id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a diary entry by ID")
    public ResponseEntity<DiaryEntryResponse> update(
            @PathVariable UUID id,
            @RequestPart(value = "diary", required = false) @Valid DiaryEntryRequest request,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> files
    ) {
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
}
