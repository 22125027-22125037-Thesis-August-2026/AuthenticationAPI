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

import com.mhsa.backend.tracking.dto.DiaryEntryRequest;
import com.mhsa.backend.tracking.dto.DiaryEntryResponse;
import com.mhsa.backend.tracking.service.DiaryEntryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

    @PostMapping("/")
    @Operation(summary = "Create diary entry", description = "Creates a new diary entry for a profile")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "CreateDiaryEntryRequest",
                            value = """
                        {
                          "profileId": "123e4567-e89b-12d3-a456-426614174000",
                          "title": "Focused and productive day",
                          "content": "Today was a productive day, I felt really focused.",
                          "positivityScore": 8,
                          "entryDate": "2026-02-28"
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
                          "profileId": "123e4567-e89b-12d3-a456-426614174000",
                          "title": "Post-workout reflection",
                          "content": "Felt great after the workout.",
                          "positivityScore": 8,
                          "entryDate": "2026-02-28",
                          "mediaAttachments": [
                            {
                              "id": "d1d3093b-c3df-4246-8301-b2ac6f6af9f3",
                              "profileId": "123e4567-e89b-12d3-a456-426614174000",
                              "referenceId": "a3f94374-03b0-4b7e-a218-53a3ad6dcc1f",
                              "referenceType": "DIARY_ENTRY",
                              "fileUrl": "https://cdn.mhsa.app/media/diary/entry-01.jpg",
                              "mediaType": "IMAGE",
                              "mimeType": "image/jpeg",
                              "fileSizeBytes": 245760,
                              "createdAt": "2026-02-28T21:22:10"
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
    public ResponseEntity<DiaryEntryResponse> create(@Valid @RequestBody DiaryEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryEntryService.create(request));
    }

    @GetMapping("/profile/{profileId}")
    @Operation(summary = "Get diary entries by profile", description = "Retrieves all diary entries for a specific profile")
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
                                                                                        "profileId": "123e4567-e89b-12d3-a456-426614174000",
                                                                                        "title": "Post-workout reflection",
                                                                                        "content": "Felt great after the workout.",
                                                                                        "positivityScore": 8,
                                                                                        "entryDate": "2026-02-28",
                                                                                        "mediaAttachments": [
                                                                                            {
                                                                                                "id": "d1d3093b-c3df-4246-8301-b2ac6f6af9f3",
                                                                                                "profileId": "123e4567-e89b-12d3-a456-426614174000",
                                                                                                "referenceId": "a3f94374-03b0-4b7e-a218-53a3ad6dcc1f",
                                                                                                "referenceType": "DIARY_ENTRY",
                                                                                                "fileUrl": "https://cdn.mhsa.app/media/diary/entry-01.jpg",
                                                                                                "mediaType": "IMAGE",
                                                                                                "mimeType": "image/jpeg",
                                                                                                "fileSizeBytes": 245760,
                                                                                                "createdAt": "2026-02-28T21:22:10"
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
        @ApiResponse(responseCode = "400", description = "Bad Request - invalid profileId", content = @Content)
    })
    public ResponseEntity<List<DiaryEntryResponse>> getAllByProfileId(@PathVariable UUID profileId) {
        return ResponseEntity.ok(diaryEntryService.getAllByProfileId(profileId));
    }
}
