package com.mhsa.backend.auth.messaging;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Payload published by therapist-api on routing key {@code therapist.assignment.changed}.
 *
 * <p>Parsed field-by-field from the JSON tree (rather than via data-binding) so {@code Instant}
 * handling never depends on a particular Jackson date-module being registered — the consumer's
 * injected {@code ObjectMapper} has none. Unknown fields are ignored; all four fields below are
 * required, and a message missing any of them is treated as malformed and dead-lettered.
 */
public record AssignmentEvent(
        UUID therapistProfileId,
        UUID patientProfileId,
        String status,
        Instant occurredAt) {

    public boolean isValid() {
        return therapistProfileId != null
                && patientProfileId != null
                && status != null
                && occurredAt != null;
    }

    /** Parses an event from its JSON body. Returns {@code null} fields for absent/unparseable keys. */
    public static AssignmentEvent fromJson(JsonNode node) {
        return new AssignmentEvent(
                JsonValues.uuid(node, "therapistProfileId"),
                JsonValues.uuid(node, "patientProfileId"),
                JsonValues.text(node, "status"),
                JsonValues.instant(node, "occurredAt"));
    }
}
