package com.mhsa.backend.auth.messaging;

import java.time.Instant;
import java.util.UUID;

import com.mhsa.backend.auth.model.DataAccessGrant;
import com.mhsa.backend.auth.model.GrantStatus;

/**
 * Internal Spring application event carrying a full snapshot of a data-access grant at the moment
 * it changed. Published from {@link com.mhsa.backend.auth.service.DataAccessGrantService} inside the
 * grant transaction and turned into a RabbitMQ message by {@link AuthEventPublisher} only
 * {@code AFTER_COMMIT}, so a rolled-back grant never emits.
 *
 * <p>{@code occurredAt} is the moment of the change and is what downstream consumers order on.
 */
public record GrantChangedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID grantId,
        UUID granterProfileId,
        UUID granteeProfileId,
        GrantStatus status,
        String accessScope,
        Instant grantedAt,
        Instant expiresAt) {

    /** Builds an event from the persisted grant, stamping a fresh event id and {@code occurredAt}. */
    public static GrantChangedEvent of(DataAccessGrant grant, Instant occurredAt) {
        return new GrantChangedEvent(
                UUID.randomUUID(),
                occurredAt,
                grant.getGrantId(),
                grant.getGranterProfileId(),
                grant.getGranteeProfileId(),
                grant.getStatus(),
                grant.getAccessScope(),
                grant.getGrantedAt(),
                grant.getExpiresAt());
    }

    /** A grant that is no longer ACTIVE is routed as a revocation downstream. */
    public boolean isActive() {
        return status == GrantStatus.ACTIVE;
    }
}
