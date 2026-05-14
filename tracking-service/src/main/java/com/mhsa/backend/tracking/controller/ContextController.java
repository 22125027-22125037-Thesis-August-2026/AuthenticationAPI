package com.mhsa.backend.tracking.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.tracking.service.ContextAggregatorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/internal/v1/tracking")
@RequiredArgsConstructor
@Slf4j
public class ContextController {

    private final ContextAggregatorService contextAggregatorService;

    @GetMapping("/context/{profileId}")
    public ResponseEntity<String> getUserContext(
            @PathVariable("profileId") UUID profileId,
            @RequestParam(defaultValue = "7") int days) {

        log.info("Fetching context for profileId: {} for last {} days", profileId, days);

        try {
            String context = contextAggregatorService.getUserContextSummary(profileId, days);
            return ResponseEntity.ok(context);
        } catch (Exception e) {
            log.error("Error fetching context for profileId: {}", profileId, e);
            return ResponseEntity.internalServerError()
                    .body("[USER CONTEXT - UNAVAILABLE]\n"
                            + "- Sleep: Unable to retrieve recent sleep data.\n"
                            + "- Mood: Unable to retrieve recent mood data.\n"
                            + "- Diet: Unable to retrieve recent diet data.\n");
        }
    }
}
