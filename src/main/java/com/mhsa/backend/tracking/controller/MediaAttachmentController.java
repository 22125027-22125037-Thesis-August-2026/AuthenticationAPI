package com.mhsa.backend.tracking.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.tracking.dto.MediaAttachmentRequest;
import com.mhsa.backend.tracking.dto.MediaAttachmentResponse;
import com.mhsa.backend.tracking.service.MediaAttachmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/media")
@RequiredArgsConstructor
@Tag(name = "Media Attachment API", description = "APIs for creating and retrieving media attachments")
public class MediaAttachmentController {

    private final MediaAttachmentService mediaAttachmentService;

    @PostMapping("/")
    @Operation(summary = "Create media attachment", description = "Creates a new media attachment for a profile")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "CreateMediaAttachmentRequest",
                            value = """
                        {
                          "profileId": "123e4567-e89b-12d3-a456-426614174000",
                          "referenceId": "4c8de90a-7f59-4906-8619-8416fd8c57d3",
                          "referenceType": "DIARY_ENTRY",
                          "fileUrl": "https://cdn.mhsa.app/media/diary/entry-01.jpg",
                          "mediaType": "IMAGE",
                          "mimeType": "image/jpeg",
                          "fileSizeBytes": 245760
                        }
                        """
                    )
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Media attachment created successfully",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "MediaAttachmentCreatedResponse",
                                value = """
                        {
                          "id": "d1d3093b-c3df-4246-8301-b2ac6f6af9f3",
                          "profileId": "123e4567-e89b-12d3-a456-426614174000",
                          "referenceId": "4c8de90a-7f59-4906-8619-8416fd8c57d3",
                          "referenceType": "DIARY_ENTRY",
                          "fileUrl": "https://cdn.mhsa.app/media/diary/entry-01.jpg",
                          "mediaType": "IMAGE",
                          "mimeType": "image/jpeg",
                          "fileSizeBytes": 245760,
                          "createdAt": "2026-02-28T21:22:10"
                        }
                        """
                        )
                )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - validation failed", content = @Content)
    })
    public ResponseEntity<MediaAttachmentResponse> create(@Valid @RequestBody MediaAttachmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaAttachmentService.create(request));
    }

    @GetMapping("/profile/{profileId}")
    @Operation(summary = "Get media attachments by profile", description = "Retrieves all media attachments for a specific profile")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Media attachments retrieved successfully",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "MediaAttachmentListResponse",
                                value = """
                        [
                          {
                            "id": "d1d3093b-c3df-4246-8301-b2ac6f6af9f3",
                            "profileId": "123e4567-e89b-12d3-a456-426614174000",
                            "referenceId": "4c8de90a-7f59-4906-8619-8416fd8c57d3",
                            "referenceType": "DIARY_ENTRY",
                            "fileUrl": "https://cdn.mhsa.app/media/diary/entry-01.jpg",
                            "mediaType": "IMAGE",
                            "mimeType": "image/jpeg",
                            "fileSizeBytes": 245760,
                            "createdAt": "2026-02-28T21:22:10"
                          }
                        ]
                        """
                        )
                )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - invalid profileId", content = @Content)
    })
    public ResponseEntity<List<MediaAttachmentResponse>> getAllByProfileId(@PathVariable UUID profileId) {
        return ResponseEntity.ok(mediaAttachmentService.getAllByProfileId(profileId));
    }
}
