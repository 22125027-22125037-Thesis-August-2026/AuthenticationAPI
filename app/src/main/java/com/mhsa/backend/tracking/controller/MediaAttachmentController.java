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
import com.mhsa.backend.tracking.dto.MediaAttachmentRequest;
import com.mhsa.backend.tracking.dto.MediaAttachmentResponse;
import com.mhsa.backend.tracking.service.MediaAttachmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/media")
@RequiredArgsConstructor
@Tag(name = "Media Attachment API")
public class MediaAttachmentController {

    private final MediaAttachmentService mediaAttachmentService;

    @PostMapping("/")
    @Operation(summary = "Create a new media attachment")
    public ResponseEntity<MediaAttachmentResponse> create(@Valid @RequestBody MediaAttachmentRequest request) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaAttachmentService.create(profileId, request));
    }

    @GetMapping("/")
    @Operation(summary = "Get all media attachments")
    public ResponseEntity<List<MediaAttachmentResponse>> getAll() {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(mediaAttachmentService.getAllByProfileId(profileId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a media attachment by ID")
    public ResponseEntity<MediaAttachmentResponse> getById(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(mediaAttachmentService.getById(profileId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a media attachment by ID")
    public ResponseEntity<MediaAttachmentResponse> update(@PathVariable UUID id, @Valid @RequestBody MediaAttachmentRequest request) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        return ResponseEntity.ok(mediaAttachmentService.update(profileId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a media attachment by ID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID profileId = SecurityUtils.getCurrentProfileId();
        mediaAttachmentService.delete(profileId, id);
        return ResponseEntity.noContent().build();
    }
}
