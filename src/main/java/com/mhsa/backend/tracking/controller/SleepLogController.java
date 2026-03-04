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

import com.mhsa.backend.tracking.dto.SleepLogRequest;
import com.mhsa.backend.tracking.dto.SleepLogResponse;
import com.mhsa.backend.tracking.service.SleepLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/sleeps")
@RequiredArgsConstructor
@Tag(name = "Sleep Tracking API", description = "APIs for creating and retrieving sleep tracking logs")
public class SleepLogController {

    private final SleepLogService sleepLogService;

    @PostMapping("/")
    @Operation(summary = "Create sleep log", description = "Creates a new sleep tracking log for a profile")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "CreateSleepLogRequest",
                            value = """
                        {
                          "bedTime": "2026-02-28T22:30:00",
                          "wakeTime": "2026-03-01T06:30:00",
                          "sleepQuality": 7,
                          "note": "Slept well with one brief wake-up."
                        }
                        """
                    )
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Sleep log created successfully",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "SleepLogCreatedResponse",
                                value = """
                        {
                          "id": "e0a4a3fb-5f2f-4061-8f23-cf2cc1773ff0",
                          "bedTime": "2026-02-28T22:30:00",
                          "wakeTime": "2026-03-01T06:30:00",
                          "durationMinutes": 480,
                          "sleepQuality": 7,
                          "note": "Slept well with one brief wake-up.",
                          "createdAt": "2026-03-01T06:31:10",
                          "updatedAt": "2026-03-01T06:31:10"
                        }
                        """
                        )
                )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - validation failed", content = @Content)
    })
    public ResponseEntity<SleepLogResponse> create(Authentication authentication, @Valid @RequestBody SleepLogRequest request) {
        UUID profileId = extractAuthenticatedProfileId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(sleepLogService.create(profileId, request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my sleep logs", description = "Retrieves all sleep logs for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Sleep logs retrieved successfully",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "SleepLogListResponse",
                                value = """
                        [
                          {
                            "id": "e0a4a3fb-5f2f-4061-8f23-cf2cc1773ff0",
                            "bedTime": "2026-02-28T22:30:00",
                            "wakeTime": "2026-03-01T06:30:00",
                            "durationMinutes": 480,
                            "sleepQuality": 7,
                            "note": "Slept well with one brief wake-up.",
                            "createdAt": "2026-03-01T06:31:10",
                            "updatedAt": "2026-03-01T06:31:10"
                          }
                        ]
                        """
                        )
                )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - invalid authenticated user id", content = @Content)
    })
    public ResponseEntity<List<SleepLogResponse>> getAllByProfileId(Authentication authentication) {
        UUID profileId = extractAuthenticatedProfileId(authentication);
        return ResponseEntity.ok(sleepLogService.getAllByProfileId(profileId));
    }

    private UUID extractAuthenticatedProfileId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authenticated user id is required");
        }
        return UUID.fromString(authentication.getName());
    }
}
