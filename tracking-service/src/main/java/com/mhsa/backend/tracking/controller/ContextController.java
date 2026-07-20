package com.mhsa.backend.tracking.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.contract.SystemProfiles;
import com.mhsa.backend.tracking.service.ContextAggregatorService;
import com.mhsa.backend.tracking.service.DataAccessGrantService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/internal/v1/tracking")
@RequiredArgsConstructor
@Slf4j
public class ContextController {

    private final ContextAggregatorService contextAggregatorService;
    private final DataAccessGrantService dataAccessGrantService;

    /**
     * The AI companion's grounding context. Consent-gated at the data owner's boundary: the caller
     * (ai-service) only ever receives tracking context for a user who has granted the reserved AI
     * companion principal access. No grant ⇒ 403, and the AI service degrades to an ungrounded reply.
     * This makes the AI just another grantee under the same model as a therapist, parent, or friend.
     */
    @GetMapping("/context/{profileId}")
    public ResponseEntity<String> getUserContext(
            @PathVariable("profileId") UUID profileId,
            @RequestParam(defaultValue = "7") int days) {

        if (!dataAccessGrantService.hasDelegatedAccess(profileId, SystemProfiles.AI_COMPANION_ID)) {
            log.info("No AI-companion data-access grant for profileId: {}; withholding context", profileId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("[USER CONTEXT - NOT SHARED]");
        }

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
