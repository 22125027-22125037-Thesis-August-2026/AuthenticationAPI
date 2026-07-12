package com.mhsa.backend.tracking.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mhsa.backend.tracking.entity.AccessScope;
import com.mhsa.backend.tracking.entity.DataAccessGrant;
import com.mhsa.backend.tracking.entity.GrantStatus;
import com.mhsa.backend.tracking.messaging.GrantSnapshotItem;
import com.mhsa.backend.tracking.repository.DataAccessGrantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Maintains the local {@code data_access_grants} read-model replica of auth-service's grants. All
 * writes to the replica funnel through here so the out-of-order guard and timestamp bookkeeping
 * live in one place.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GrantReplicaService {

    private final DataAccessGrantRepository repository;

    /** Outcome of applying an event, for caller-side logging/metrics. */
    public enum ApplyResult {
        APPLIED,
        DROPPED_STALE
    }

    /**
     * Idempotent upsert from a grant event, keyed by {@code grantId} (falling back to the
     * (granter, grantee) pair when no id is present).
     *
     * <p>Applied only when {@code occurredAt >= last_event_at}; an older/replayed event is dropped
     * so stale state can never overwrite newer state. On apply, {@code occurredAt} becomes the new
     * {@code last_event_at} watermark.
     */
    @Transactional
    public ApplyResult applyEvent(UUID grantId, UUID granterProfileId, UUID granteeProfileId,
                                  GrantStatus status, AccessScope accessScope,
                                  Instant grantedAt, Instant expiresAt, Instant occurredAt) {
        DataAccessGrant row = find(grantId, granterProfileId, granteeProfileId);

        if (row != null && row.getLastEventAt() != null && occurredAt.isBefore(row.getLastEventAt())) {
            log.debug("Dropping stale grant event: grantId={}, granter={}, grantee={}, occurredAt={} < lastEventAt={}",
                    grantId, granterProfileId, granteeProfileId, occurredAt, row.getLastEventAt());
            return ApplyResult.DROPPED_STALE;
        }

        if (row == null) {
            row = DataAccessGrant.builder()
                    // Preserve auth's id; only mint a surrogate if an id-less event ever arrives.
                    .grantId(grantId != null ? grantId : UUID.randomUUID())
                    .granterProfileId(granterProfileId)
                    .granteeProfileId(granteeProfileId)
                    .grantedAt(grantedAt != null ? grantedAt : occurredAt)
                    .accessScope(accessScope != null ? accessScope : AccessScope.READ_JOURNAL)
                    .build();
        } else {
            if (granterProfileId != null) {
                row.setGranterProfileId(granterProfileId);
            }
            if (granteeProfileId != null) {
                row.setGranteeProfileId(granteeProfileId);
            }
            if (grantedAt != null) {
                row.setGrantedAt(grantedAt);
            }
            if (accessScope != null) {
                row.setAccessScope(accessScope);
            }
        }
        row.setStatus(status);
        row.setExpiresAt(expiresAt);
        row.setLastEventAt(occurredAt);
        repository.save(row);

        log.info("Applied grant event: grantId={}, granter={}, grantee={}, status={}, occurredAt={}",
                row.getGrantId(), row.getGranterProfileId(), row.getGranteeProfileId(), status, occurredAt);
        return ApplyResult.APPLIED;
    }

    /**
     * Atomically converges the replica to a validated auth-service snapshot (nightly reconciliation).
     * Every snapshot row is upserted to its snapshot status; locally-ACTIVE rows whose key is absent
     * from the snapshot are marked REVOKED. Unlike {@link #applyEvent} this trusts the snapshot as
     * authoritative and does not apply the watermark guard, but it leaves an existing
     * {@code last_event_at} untouched so later event replays are still rejected correctly.
     *
     * <p>The caller must validate the snapshot (non-empty, every status parseable) before calling.
     *
     * @return {@code [upserted, revoked]} counts.
     */
    @Transactional
    public int[] reconcileSnapshot(List<GrantSnapshotItem> snapshot) {
        Instant now = Instant.now();
        Set<UUID> snapshotIds = new HashSet<>();
        int upserted = 0;

        for (GrantSnapshotItem item : snapshot) {
            GrantStatus status = parseStatus(item.status());
            if (status == null) {
                continue;
            }
            AccessScope scope = parseScope(item.accessScope());

            DataAccessGrant row = find(item.grantId(), item.granterProfileId(), item.granteeProfileId());
            if (row == null) {
                row = DataAccessGrant.builder()
                        .grantId(item.grantId() != null ? item.grantId() : UUID.randomUUID())
                        .granterProfileId(item.granterProfileId())
                        .granteeProfileId(item.granteeProfileId())
                        .grantedAt(item.grantedAt() != null ? item.grantedAt() : now)
                        .accessScope(scope != null ? scope : AccessScope.READ_JOURNAL)
                        .build();
            } else {
                if (item.granterProfileId() != null) {
                    row.setGranterProfileId(item.granterProfileId());
                }
                if (item.granteeProfileId() != null) {
                    row.setGranteeProfileId(item.granteeProfileId());
                }
                if (item.grantedAt() != null) {
                    row.setGrantedAt(item.grantedAt());
                }
                if (scope != null) {
                    row.setAccessScope(scope);
                }
            }
            row.setStatus(status);
            row.setExpiresAt(item.expiresAt());
            repository.save(row);
            snapshotIds.add(row.getGrantId());
            upserted++;
        }

        int revoked = 0;
        for (DataAccessGrant row : repository.findByStatus(GrantStatus.ACTIVE)) {
            if (!snapshotIds.contains(row.getGrantId())) {
                row.setStatus(GrantStatus.REVOKED);
                repository.save(row);
                revoked++;
            }
        }

        return new int[] {upserted, revoked};
    }

    private DataAccessGrant find(UUID grantId, UUID granterProfileId, UUID granteeProfileId) {
        if (grantId != null) {
            DataAccessGrant byId = repository.findById(grantId).orElse(null);
            if (byId != null) {
                return byId;
            }
        }
        if (granterProfileId != null && granteeProfileId != null) {
            return repository
                    .findByGranterProfileIdAndGranteeProfileId(granterProfileId, granteeProfileId)
                    .orElse(null);
        }
        return null;
    }

    private static GrantStatus parseStatus(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return GrantStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static AccessScope parseScope(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return AccessScope.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
