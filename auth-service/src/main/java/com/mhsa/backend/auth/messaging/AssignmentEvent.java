package com.mhsa.backend.auth.messaging;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload published by therapist-api on routing key {@code therapist.assignment.changed}.
 *
 * <p>Unknown fields are ignored so additive changes on therapist-api's side don't poison the
 * queue. All four fields are required; a message missing any of them is treated as malformed
 * and dead-lettered by the consumer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
}
