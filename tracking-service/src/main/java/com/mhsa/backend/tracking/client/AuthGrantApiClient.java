package com.mhsa.backend.tracking.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.mhsa.backend.tracking.messaging.GrantSnapshotItem;

import jakarta.annotation.PostConstruct;

/**
 * Thin client over auth-service's {@code GET /internal/grants} snapshot endpoint, used by nightly
 * reconciliation. Pages through the full result set.
 *
 * <p>Deliberately <b>fails loud</b>: any HTTP/parse error propagates so the reconciliation job can
 * abort without mutating the replica. The response is read as a JSON tree to tolerate either a
 * Spring-Data {@code Page} envelope ({@code {"content":[...],"last":...}}) or a bare array.
 */
@Component
public class AuthGrantApiClient {

    private static final int MAX_PAGES = 10_000; // safety valve against a misbehaving "last" flag.

    @Value("${auth.api.base-url:http://auth-service:8081}")
    private String baseUrl;

    @Value("${auth.api.grants-path:/internal/grants}")
    private String grantsPath;

    @Value("${auth.api.page-size:200}")
    private int pageSize;

    private RestClient restClient;

    @PostConstruct
    void init() {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Fetches every grant in the snapshot. Throws on any failure (network, non-2xx, parse) so
     * callers can treat a thrown exception as "do not touch the replica".
     */
    public List<GrantSnapshotItem> fetchAllGrants() {
        List<GrantSnapshotItem> all = new ArrayList<>();

        for (int page = 0; page < MAX_PAGES; page++) {
            final int p = page;
            JsonNode root = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(grantsPath)
                            .queryParam("page", p)
                            .queryParam("size", pageSize)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (root == null) {
                throw new IllegalStateException("auth-service returned empty response body for page " + p);
            }

            JsonNode content = root.isArray() ? root : root.path("content");
            if (!content.isArray()) {
                throw new IllegalStateException("auth-service snapshot has no 'content' array on page " + p);
            }

            for (JsonNode item : content) {
                all.add(GrantSnapshotItem.fromJson(item));
            }

            if (isLastPage(root, content)) {
                break;
            }
        }

        return all;
    }

    private boolean isLastPage(JsonNode root, JsonNode content) {
        if (root.isArray()) {
            return true;
        }
        if (root.hasNonNull("last")) {
            return root.get("last").asBoolean();
        }
        return content.size() < pageSize;
    }
}
