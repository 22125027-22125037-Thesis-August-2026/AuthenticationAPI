package com.mhsa.backend.auth.messaging;

import java.time.Instant;
import java.util.UUID;

import com.mhsa.backend.auth.model.TherapistProfile;

/**
 * Internal Spring application event carrying a snapshot of a therapist profile at the moment it
 * changed (profile edit, avatar change, or license transition). Published inside the editing
 * transaction and turned into a {@code therapist.profile.updated} RabbitMQ message by
 * {@link AuthEventPublisher} only {@code AFTER_COMMIT}, so a rolled-back edit never emits.
 *
 * <p>{@code occurredAt} is the moment of the change and is what downstream consumers order on.
 */
public record TherapistProfileChangedEvent(
        UUID eventId,
        Instant occurredAt,
        TherapistProfileView profile) {

    /** Builds an event from the persisted therapist, stamping a fresh event id and {@code occurredAt}. */
    public static TherapistProfileChangedEvent of(TherapistProfile therapist, Instant occurredAt) {
        return new TherapistProfileChangedEvent(
                UUID.randomUUID(),
                occurredAt,
                TherapistProfileView.from(therapist));
    }

    public UUID profileId() {
        return profile == null ? null : profile.profileId();
    }
}
