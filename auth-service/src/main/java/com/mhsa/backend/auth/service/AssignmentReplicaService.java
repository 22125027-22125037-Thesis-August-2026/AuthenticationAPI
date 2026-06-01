package com.mhsa.backend.auth.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mhsa.backend.auth.messaging.AssignmentSnapshotItem;
import com.mhsa.backend.auth.model.AssignmentStatus;
import com.mhsa.backend.auth.model.TherapistPatientAssignment;
import com.mhsa.backend.auth.model.TherapistPatientAssignmentId;
import com.mhsa.backend.auth.repository.TherapistPatientAssignmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Maintains the local {@code therapist_patient_assignment} read-model. All writes to the replica
 * funnel through here so the out-of-order guard and timestamp bookkeeping live in one place.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentReplicaService {

    private final TherapistPatientAssignmentRepository repository;

    /** Outcome of applying an event, for caller-side logging/metrics. */
    public enum ApplyResult {
        /** Row created or updated from this event. */
        APPLIED,
        /** Event was older than the row's watermark and was ignored. */
        DROPPED_STALE
    }

    /**
     * Idempotent upsert from an assignment-change event, keyed by (therapist, patient).
     *
     * <p>Applied only when {@code occurredAt >= last_event_at}; an older/replayed event is dropped
     * so a stale message can never overwrite newer state. On apply, {@code occurredAt} becomes the
     * new {@code last_event_at} watermark and {@code status} is mapped straight through.
     */
    @Transactional
    public ApplyResult applyEvent(UUID therapistProfileId, UUID patientProfileId,
                                  AssignmentStatus status, Instant occurredAt) {
        TherapistPatientAssignmentId id =
                new TherapistPatientAssignmentId(therapistProfileId, patientProfileId);

        TherapistPatientAssignment row = repository.findById(id).orElse(null);

        if (row != null && row.getLastEventAt() != null && occurredAt.isBefore(row.getLastEventAt())) {
            log.debug("Dropping stale assignment event: therapist={}, patient={}, occurredAt={} < lastEventAt={}",
                    therapistProfileId, patientProfileId, occurredAt, row.getLastEventAt());
            return ApplyResult.DROPPED_STALE;
        }

        Instant now = Instant.now();
        if (row == null) {
            row = TherapistPatientAssignment.builder()
                    .id(id)
                    .assignedAt(occurredAt)
                    .build();
        }
        row.setStatus(status);
        row.setLastEventAt(occurredAt);
        row.setUpdatedAt(now);
        repository.save(row);

        log.info("Applied assignment event: therapist={}, patient={}, status={}, occurredAt={}",
                therapistProfileId, patientProfileId, status, occurredAt);
        return ApplyResult.APPLIED;
    }

    /**
     * Atomically converges the replica to a validated therapist-api snapshot (used by nightly
     * reconciliation). Every snapshot row is upserted to its snapshot status; locally-ACTIVE rows
     * absent from the snapshot are marked INACTIVE. Unlike {@link #applyEvent}, this trusts the
     * snapshot as authoritative and does not apply the out-of-order watermark guard, but it leaves
     * an existing {@code last_event_at} untouched so later replays are still rejected correctly.
     *
     * <p>The caller must validate the snapshot (non-empty, every status parseable) before calling;
     * statuses here are parsed permissively and an unparseable one is skipped defensively.
     *
     * @return {@code [upserted, deactivated]} counts.
     */
    @Transactional
    public int[] reconcileSnapshot(List<AssignmentSnapshotItem> snapshot) {
        Instant now = Instant.now();
        Set<TherapistPatientAssignmentId> snapshotKeys = new HashSet<>();
        int upserted = 0;

        for (AssignmentSnapshotItem item : snapshot) {
            AssignmentStatus status = parseStatus(item.status());
            if (status == null) {
                continue;
            }
            TherapistPatientAssignmentId id =
                    new TherapistPatientAssignmentId(item.therapistProfileId(), item.patientProfileId());
            snapshotKeys.add(id);

            TherapistPatientAssignment row = repository.findById(id).orElse(null);
            if (row == null) {
                row = TherapistPatientAssignment.builder()
                        .id(id)
                        .assignedAt(item.assignedAt() != null ? item.assignedAt() : now)
                        .lastEventAt(now)
                        .build();
            } else if (row.getAssignedAt() == null && item.assignedAt() != null) {
                row.setAssignedAt(item.assignedAt());
            }
            row.setStatus(status);
            row.setUpdatedAt(now);
            repository.save(row);
            upserted++;
        }

        int deactivated = 0;
        for (TherapistPatientAssignment row : repository.findByStatus(AssignmentStatus.ACTIVE)) {
            if (!snapshotKeys.contains(row.getId())) {
                row.setStatus(AssignmentStatus.INACTIVE);
                row.setUpdatedAt(now);
                repository.save(row);
                deactivated++;
            }
        }

        return new int[] {upserted, deactivated};
    }

    private static AssignmentStatus parseStatus(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return AssignmentStatus.valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
