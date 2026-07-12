package com.mhsa.backend.auth.messaging;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Null-tolerant extractors for pulling typed values out of a Jackson {@link JsonNode}. Used instead
 * of data-binding so {@code Instant}/{@code UUID} parsing is independent of any Jackson module
 * configuration (the injected {@code ObjectMapper} here has no JSR-310 module registered). A
 * missing, null, or unparseable field yields {@code null} rather than throwing, leaving validity
 * decisions to the caller.
 */
final class JsonValues {

    private JsonValues() {
    }

    static String text(JsonNode node, String field) {
        JsonNode v = node == null ? null : node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        String s = v.asText();
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    static UUID uuid(JsonNode node, String field) {
        String s = text(node, field);
        if (s == null) {
            return null;
        }
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static Instant instant(JsonNode node, String field) {
        String s = text(node, field);
        if (s == null) {
            return null;
        }
        try {
            return Instant.parse(s);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
