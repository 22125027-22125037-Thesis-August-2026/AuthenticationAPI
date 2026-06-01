package com.mhsa.backend.tracking.messaging;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Enriched data-access-grant event published by auth-service on routing keys
 * {@code auth.grant.created} / {@code auth.grant.revoked}.
 *
 * <p>Parsed field-by-field from the JSON tree (rather than via data-binding) so {@code Instant}
 * handling never depends on a particular Jackson date-module being registered. A message that is
 * missing the identifying/ordering fields is invalid and is dead-lettered by the consumer.
 */
public record GrantEvent(
        UUID eventId,
        Instant occurredAt,
        UUID grantId,
        UUID granterProfileId,
        UUID granteeProfileId,
        String status,
        String accessScope,
        Instant grantedAt,
        Instant expiresAt) {

    public boolean isValid() {
        // grantId is the preferred key; fall back to the (granter, grantee) pair. occurredAt drives
        // the watermark, and status tells us whether access is on or off — all are required.
        boolean hasKey = grantId != null || (granterProfileId != null && granteeProfileId != null);
        return hasKey && occurredAt != null && status != null;
    }

    /** Parses an enriched event from its JSON body. Returns {@code null} fields for absent keys. */
    public static GrantEvent fromJson(JsonNode node) {
        return new GrantEvent(
                JsonValues.uuid(node, "eventId"),
                JsonValues.instant(node, "occurredAt"),
                JsonValues.uuid(node, "grantId"),
                JsonValues.uuid(node, "granterProfileId"),
                JsonValues.uuid(node, "granteeProfileId"),
                JsonValues.text(node, "status"),
                JsonValues.text(node, "accessScope"),
                JsonValues.instant(node, "grantedAt"),
                JsonValues.instant(node, "expiresAt"));
    }
}
