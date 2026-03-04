package com.mhsa.backend.tracking.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.tracking.dto.MoodLogRequest;
import com.mhsa.backend.tracking.dto.MoodLogResponse;
import com.mhsa.backend.tracking.service.MoodLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/moods")
@RequiredArgsConstructor
@Tag(name = "Mood Tracking API", description = "APIs for creating and retrieving mood tracking logs")
public class MoodLogController {

    private final MoodLogService moodLogService;

    @PostMapping("/")
    @Operation(summary = "Create mood log", description = "Creates a new mood tracking log for a profile")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "CreateMoodLogRequest",
                            value = """
                        {
                          "positivityScore": 8,
                                                                                                        "note": "Felt calm after a short walk."
                        }
                        """
                    )
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Mood log created successfully",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "MoodLogCreatedResponse",
                                value = """
                        {
                          "id": "8b2af1c7-fd57-4695-ac34-7c915600fd2f",
                          "positivityScore": 8,
                          "note": "Felt calm after a short walk.",
                                                                                                                                                                        "logDate": "2026-02-28T20:15:00"
                        }
                        """
                        )
                )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - validation failed", content = @Content)
    })
    public ResponseEntity<MoodLogResponse> create(Authentication authentication, @Valid @RequestBody MoodLogRequest request) {
        UUID profileId = extractAuthenticatedProfileId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(moodLogService.create(profileId, request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my mood logs", description = "Retrieves all mood logs for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Mood logs retrieved successfully",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "MoodLogListResponse",
                                value = """
                        [
                          {
                            "id": "8b2af1c7-fd57-4695-ac34-7c915600fd2f",
                            "positivityScore": 8,
                            "note": "Felt calm after a short walk.",
                                                                                                                                                                                "logDate": "2026-02-28T20:15:00"
                          }
                        ]
                        """
                        )
                )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - invalid authenticated user id", content = @Content)
    })
    public ResponseEntity<List<MoodLogResponse>> getAllByProfileId(Authentication authentication) {
        UUID profileId = extractAuthenticatedProfileId(authentication);
        return ResponseEntity.ok(moodLogService.getAllByProfileId(profileId));
    }

    private UUID extractAuthenticatedProfileId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authenticated user id is required");
        }
        return UUID.fromString(authentication.getName());
    }
}
