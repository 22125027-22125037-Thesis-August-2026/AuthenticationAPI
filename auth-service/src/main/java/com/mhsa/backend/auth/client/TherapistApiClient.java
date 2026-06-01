package com.mhsa.backend.auth.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhsa.backend.auth.messaging.AssignmentSnapshotItem;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Thin client over therapist-api's {@code GET /internal/assignments} snapshot endpoint, used by
 * nightly reconciliation. Pages through the full result set.
 *
 * <p>Deliberately <b>fails loud</b>: any HTTP/parse error propagates so the reconciliation job can
 * abort without mutating the replica. The response is read as a JSON tree to tolerate either a
 * Spring-Data {@code Page} envelope ({@code {"content":[...],"last":...}}) or a bare array.
 */
@Component
@Slf4j
public class TherapistApiClient {

    private static final int MAX_PAGES = 10_000; // safety valve against a misbehaving "last" flag.

    @Value("${therapist.api.base-url:http://therapist-api:8080}")
    private String baseUrl;

    @Value("${therapist.api.assignments-path:/internal/assignments}")
    private String assignmentsPath;

    @Value("${therapist.api.page-size:200}")
    private int pageSize;

    private final ObjectMapper objectMapper;
    private RestClient restClient;

    public TherapistApiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Fetches every assignment in the snapshot. Throws on any failure (network, non-2xx, parse) so
     * callers can treat a thrown exception as "do not touch the replica".
     */
    public List<AssignmentSnapshotItem> fetchAllAssignments() {
        List<AssignmentSnapshotItem> all = new ArrayList<>();

        for (int page = 0; page < MAX_PAGES; page++) {
            final int p = page;
            JsonNode root = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(assignmentsPath)
                            .queryParam("page", p)
                            .queryParam("size", pageSize)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (root == null) {
                throw new IllegalStateException("therapist-api returned empty response body for page " + p);
            }

            JsonNode content = root.isArray() ? root : root.path("content");
            if (!content.isArray()) {
                throw new IllegalStateException("therapist-api snapshot has no 'content' array on page " + p);
            }

            for (JsonNode node : content) {
                all.add(objectMapper.convertValue(node, AssignmentSnapshotItem.class));
            }

            if (isLastPage(root, content)) {
                break;
            }
        }

        return all;
    }

    private boolean isLastPage(JsonNode root, JsonNode content) {
        // Bare array => single page.
        if (root.isArray()) {
            return true;
        }
        // Honour an explicit Spring-Data 'last' flag when present.
        if (root.hasNonNull("last")) {
            return root.get("last").asBoolean();
        }
        // Otherwise infer: a short/empty page is the last one.
        return content.size() < pageSize;
    }
}
