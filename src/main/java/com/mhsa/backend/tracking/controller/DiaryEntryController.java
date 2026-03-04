package com.mhsa.backend.tracking.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mhsa.backend.tracking.dto.DiaryEntryRequest;
import com.mhsa.backend.tracking.dto.DiaryEntryResponse;
import com.mhsa.backend.tracking.service.DiaryEntryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/diaries")
@RequiredArgsConstructor
@Tag(name = "Diary Management API", description = "APIs for creating and retrieving diary entries")
public class DiaryEntryController {

    private final DiaryEntryService diaryEntryService;

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create diary entry", description = "Creates a new diary entry and uploads optional attachments atomically")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = {
                        @Encoding(name = "diary", contentType = "application/json"),
                        @Encoding(name = "attachments", contentType = "application/octet-stream")
                    },
                    examples = @ExampleObject(
                            name = "CreateDiaryEntryRequest",
                            value = """
                        {
                                                    "diary": {
                                                        "content": "Today was a productive day, I felt really focused.",
                                                        "moodTag": "MOTIVATED",
                                                        "positivityScore": 8
                                                    },
                                                    "attachments": ["<binary-file-1>", "<binary-file-2>"]
                        }
                        """
                    )
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Diary entry created successfully",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "DiaryEntryCreatedResponse",
                                value = """
                        {
                          "id": "a3f94374-03b0-4b7e-a218-53a3ad6dcc1f",
                          "content": "Felt great after the workout.",
                                                    "moodTag": "MOTIVATED",
                          "positivityScore": 8,
                                                    "attachments": [
                            {
                              "id": "d1d3093b-c3df-4246-8301-b2ac6f6af9f3",
                                                            "fileName": "entry-photo.jpg",
                                                            "fileType": "image/jpeg",
                                                            "fileUrl": "https://cdn.mhsa.app/media/diary/entry-01.jpg"
                            }
                          ],
                          "createdAt": "2026-02-28T21:12:20",
                          "updatedAt": "2026-02-28T21:12:20"
                        }
                        """
                        )
                )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - validation failed", content = @Content)
    })
    public ResponseEntity<DiaryEntryResponse> create(
            @Parameter(
                    description = "Diary metadata JSON part",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiaryEntryRequest.class))
            )
            @RequestPart("diary") @Valid DiaryEntryRequest request,
            @Parameter(
                    description = "Optional attachment files",
                    content = @Content(array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            )
            @RequestPart(value = "attachments", required = false) List<MultipartFile> files,
            Authentication authentication
    ) {
        UUID profileId = extractAuthenticatedProfileId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryEntryService.create(profileId, request, files));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my diary entries", description = "Retrieves all diary entries for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Diary entries retrieved successfully",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "DiaryEntryListResponse",
                                value = """
                                                                                [
                                                                                    {
                                                                                        "id": "a3f94374-03b0-4b7e-a218-53a3ad6dcc1f",
                                                                                        "content": "Felt great after the workout.",
                                                                                        "moodTag": "MOTIVATED",
                                                                                        "positivityScore": 8,
                                                                                        "attachments": [
                                                                                            {
                                                                                                "id": "d1d3093b-c3df-4246-8301-b2ac6f6af9f3",
                                                                                                "fileName": "entry-photo.jpg",
                                                                                                "fileType": "image/jpeg",
                                                                                                "fileUrl": "https://cdn.mhsa.app/media/diary/entry-01.jpg"
                                                                                            }
                                                                                        ],
                                                                                        "createdAt": "2026-02-28T21:12:20",
                                                                                        "updatedAt": "2026-02-28T21:20:00"
                                                                                    }
                                                                                ]
                                                                                """
                        )
                )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - invalid authenticated user id", content = @Content)
    })
    public ResponseEntity<List<DiaryEntryResponse>> getAllByProfileId(Authentication authentication) {
        UUID profileId = extractAuthenticatedProfileId(authentication);
        return ResponseEntity.ok(diaryEntryService.getAllByProfileId(profileId));
    }

    private UUID extractAuthenticatedProfileId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authenticated user id is required");
        }
        return UUID.fromString(authentication.getName());
    }
}
