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
public class AiClient {

    private final RestClient restClient;
    private static final String AI_SERVICE_URL = "http://ai-service:8082";
    private static final int TIMEOUT_MS = 2000;

    public JsonNode getDashboardStats(UUID profileId) {
        try {
            return restClient
                    .get()
                    .uri(AI_SERVICE_URL + "/internal/v1/dashboard/{profileId}/chat-stats", profileId)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            log.error("Error calling ai-service for dashboard stats: profileId={}", profileId, e);
            return null;
        }
    }
}
