package com.mhsa.backend.tracking.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.tracking.dto.StreakRequest;
import com.mhsa.backend.tracking.dto.StreakResponse;
import com.mhsa.backend.tracking.service.StreakService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/streaks")
@RequiredArgsConstructor
@Tag(name = "Gamification & Streak API", description = "APIs for managing and retrieving user streaks")
public class StreakController {

    private final StreakService streakService;

    @PostMapping("/")
    @Operation(summary = "Create streak record", description = "Creates a new streak record for a profile")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "CreateStreakRequest",
                            value = """
                        {
                          "profileId": "123e4567-e89b-12d3-a456-426614174000",
                          "streakType": "DAILY_TRACKING",
                          "currentCount": 5,
                          "longestCount": 12,
                          "lastLoggedAt": "2026-02-28T21:30:00"
                        }
                        """
                    )
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Streak record created successfully",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "StreakCreatedResponse",
                                value = """
                        {
                          "id": "4f927df8-d57f-4475-bf5e-cf8445b1c7f4",
                          "profileId": "123e4567-e89b-12d3-a456-426614174000",
                          "streakType": "DAILY_TRACKING",
                          "currentCount": 5,
                          "longestCount": 12,
                          "lastLoggedAt": "2026-02-28T21:30:00",
                          "createdAt": "2026-02-20T08:00:00",
                          "updatedAt": "2026-02-28T21:30:00"
                        }
                        """
                        )
                )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - validation failed", content = @Content)
    })
    public ResponseEntity<StreakResponse> create(@Valid @RequestBody StreakRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(streakService.create(request));
    }

    @GetMapping("/profile/{profileId}")
    @Operation(summary = "Get current streak by profile", description = "Retrieves the current streak information for a specific profile")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Streak retrieved successfully",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "StreakResponse",
                                value = """
                        {
                          "id": "4f927df8-d57f-4475-bf5e-cf8445b1c7f4",
                          "profileId": "123e4567-e89b-12d3-a456-426614174000",
                          "streakType": "DAILY_TRACKING",
                          "currentCount": 6,
                          "longestCount": 12,
                          "lastLoggedAt": "2026-02-28T21:30:00",
                          "createdAt": "2026-02-20T08:00:00",
                          "updatedAt": "2026-02-28T21:30:00"
                        }
                        """
                        )
                )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - invalid profileId", content = @Content)
    })
    public ResponseEntity<StreakResponse> getByProfileId(@PathVariable UUID profileId) {
        return ResponseEntity.ok(streakService.getByProfileId(profileId));
    }
}
