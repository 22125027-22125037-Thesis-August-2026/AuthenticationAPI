package com.mhsa.backend.tracking.messaging;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * One grant row from auth-service's {@code GET /internal/grants} snapshot, used by nightly
 * reconciliation. Contains both ACTIVE and REVOKED grants so the reconciler can converge exactly.
 */
public record GrantSnapshotItem(
        UUID grantId,
        UUID granterProfileId,
        UUID granteeProfileId,
        String status,
        String accessScope,
        Instant grantedAt,
        Instant expiresAt) {

    public boolean isValid() {
        boolean hasKey = grantId != null || (granterProfileId != null && granteeProfileId != null);
        return hasKey && status != null;
    }

    public static GrantSnapshotItem fromJson(JsonNode node) {
        return new GrantSnapshotItem(
                JsonValues.uuid(node, "grantId"),
                JsonValues.uuid(node, "granterProfileId"),
                JsonValues.uuid(node, "granteeProfileId"),
                JsonValues.text(node, "status"),
                JsonValues.text(node, "accessScope"),
                JsonValues.instant(node, "grantedAt"),
                JsonValues.instant(node, "expiresAt"));
    }
}
