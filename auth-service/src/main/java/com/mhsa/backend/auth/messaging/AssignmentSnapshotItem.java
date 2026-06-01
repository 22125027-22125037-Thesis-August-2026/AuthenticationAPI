package com.mhsa.backend.auth.messaging;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * One assignment row as returned by therapist-api's {@code GET /internal/assignments} snapshot,
 * used by nightly reconciliation. {@code assignedAt} is optional.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AssignmentSnapshotItem(
        UUID therapistProfileId,
        UUID patientProfileId,
        String status,
        Instant assignedAt) {

    public boolean isValid() {
        return therapistProfileId != null && patientProfileId != null && status != null;
    }
}
