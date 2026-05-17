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
public class AuthClient {

    private final RestClient restClient;
    private static final String AUTH_SERVICE_URL = "http://auth-service:8081";
    private static final int TIMEOUT_MS = 2000;

    public JsonNode getProfileSummary(UUID profileId) {
        try {
            return restClient
                    .get()
                    .uri(AUTH_SERVICE_URL + "/internal/v1/profile/{profileId}/summary", profileId)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            log.error("Error calling auth-service for profile summary: profileId={}", profileId, e);
            return null;
        }
    }
}
