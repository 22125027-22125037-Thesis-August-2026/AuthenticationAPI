package com.mhsa.backend.dashboard.client;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrackingClient {

    private final RestClient restClient;
    private static final String TRACKING_SERVICE_URL = "http://tracking-service:8083";
    private static final int TIMEOUT_MS = 2000;

    public JsonNode getDashboardSummary(UUID profileId) {
        try {
            return restClient
                    .get()
                    .uri(TRACKING_SERVICE_URL + "/internal/v1/dashboard/{profileId}/summary", profileId)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            log.error("Error calling tracking-service for dashboard summary: profileId={}", profileId, e);
            return null;
        }
    }

    public JsonNode getUserContext(UUID profileId, int days) {
        try {
            return restClient
                    .get()
                    .uri(TRACKING_SERVICE_URL + "/internal/v1/tracking/context/{profileId}?days={days}",
                            profileId, days)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            log.error("Error calling tracking-service for context: profileId={}, days={}", profileId, days, e);
            return null;
        }
    }
}
