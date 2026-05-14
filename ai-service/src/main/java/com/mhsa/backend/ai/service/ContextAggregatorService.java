package com.mhsa.backend.ai.service;

import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextAggregatorService {

    private final RestTemplate restTemplate;

    @Value("${service.tracking.url:http://backend-app:8080}")
    private String trackingServiceUrl;

    public String getUserContextSummary(UUID profileId) {
        try {
            String url = trackingServiceUrl + "/internal/v1/tracking/context/{profileId}?days=7";
            String contextData = restTemplate.getForObject(url, String.class, profileId);

            if (contextData == null || contextData.isBlank()) {
                log.warn("Tracking service returned empty context for profileId: {}", profileId);
                return buildDefaultContext();
            }

            return contextData;
        } catch (RestClientException e) {
            log.warn("Failed to fetch context from tracking-service for profileId: {}", profileId, e);
            return buildDefaultContext();
        } catch (Exception e) {
            log.error("Unexpected error fetching context from tracking-service", e);
            return buildDefaultContext();
        }
    }

    private String buildDefaultContext() {
        return "[USER CONTEXT - UNAVAILABLE]\n"
                + "- Sleep: Unable to retrieve recent sleep data.\n"
                + "- Mood: Unable to retrieve recent mood data.\n"
                + "- Diet: Unable to retrieve recent diet data.\n"
                + "- Hydration: Unable to retrieve recent hydration data.\n";
    }
}
