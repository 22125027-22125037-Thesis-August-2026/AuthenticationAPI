package com.mhsa.backend.dashboard.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mhsa.backend.dashboard.client.AiClient;
import com.mhsa.backend.dashboard.client.AuthClient;
import com.mhsa.backend.dashboard.client.TrackingClient;
import com.mhsa.backend.dashboard.dto.DashboardSummaryResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final AuthClient authClient;
    private final TrackingClient trackingClient;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public DashboardSummaryResponse getDashboardSummary(UUID profileId) {
        long startTime = System.currentTimeMillis();

        // Call 3 services in parallel using CompletableFuture
        CompletableFuture<JsonNode> authFuture = CompletableFuture.supplyAsync(() ->
                authClient.getProfileSummary(profileId)
        );

        CompletableFuture<JsonNode> trackingFuture = CompletableFuture.supplyAsync(() ->
                trackingClient.getDashboardSummary(profileId)
        );

        CompletableFuture<JsonNode> aiFuture = CompletableFuture.supplyAsync(() ->
                aiClient.getDashboardStats(profileId)
        );

        // Wait for all futures to complete (with timeout handling)
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(authFuture, trackingFuture, aiFuture);

        try {
            allFutures.join();
        } catch (Exception e) {
            log.error("Error waiting for service responses: profileId={}", profileId, e);
        }

        // Aggregate results
        JsonNode authData = authFuture.getNow(null);
        JsonNode trackingData = trackingFuture.getNow(null);
        JsonNode aiData = aiFuture.getNow(null);

        long latency = System.currentTimeMillis() - startTime;
        log.info("Dashboard aggregation completed: profileId={}, latency={}ms", profileId, latency);

        return DashboardSummaryResponse.builder()
                .profileId(profileId)
                .auth(authData)
                .tracking(trackingData)
                .ai(aiData)
                .latencyMs(latency)
                .build();
    }

    public JsonNode getUserContext(UUID profileId, int days) {
        return trackingClient.getUserContext(profileId, days);
    }

    public ObjectNode getServiceHealth() {
        ObjectNode health = objectMapper.createObjectNode();

        // Quick health check calls
        CompletableFuture<Boolean> authHealth = CompletableFuture.supplyAsync(() -> {
            try {
                JsonNode response = authClient.getProfileSummary(UUID.randomUUID());
                return response != null;
            } catch (Exception e) {
                return false;
            }
        });

        CompletableFuture<Boolean> trackingHealth = CompletableFuture.supplyAsync(() -> {
            try {
                JsonNode response = trackingClient.getDashboardSummary(UUID.randomUUID());
                return response != null;
            } catch (Exception e) {
                return false;
            }
        });

        CompletableFuture<Boolean> aiHealth = CompletableFuture.supplyAsync(() -> {
            try {
                JsonNode response = aiClient.getDashboardStats(UUID.randomUUID());
                return response != null;
            } catch (Exception e) {
                return false;
            }
        });

        try {
            health.put("auth-service", authHealth.get());
            health.put("tracking-service", trackingHealth.get());
            health.put("ai-service", aiHealth.get());
        } catch (Exception e) {
            log.error("Error checking service health", e);
            health.put("error", "Failed to check service health");
        }

        return health;
    }
}
