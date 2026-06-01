package com.mhsa.backend.auth.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Local read-model replica of a therapist&lt;-&gt;patient assignment owned by therapist-api.
 *
 * <p>This table is never written by user-facing flows. It is fed by the durable assignment-event
 * consumer and healed nightly by reconciliation against therapist-api's snapshot. It exists solely
 * so {@code AccessGuard.canViewPatientProfile} can authorize a therapist's read of a patient
 * profile without a synchronous cross-service call on the hot path.
 */
@Entity
@Table(name = "therapist_patient_assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TherapistPatientAssignment {

    @EmbeddedId
    private TherapistPatientAssignmentId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status;

    /** When therapist-api first established the assignment (best-effort, may be null). */
    @Column(name = "assigned_at")
    private Instant assignedAt;

    /**
     * {@code occurredAt} of the most recent event applied to this row. Acts as the watermark
     * for out-of-order / replayed event rejection: an incoming event is applied only when its
     * {@code occurredAt >= last_event_at}.
     */
    @Column(name = "last_event_at")
    private Instant lastEventAt;

    /** Wall-clock time this row was last touched (by an event or reconciliation). */
    @Column(name = "updated_at")
    private Instant updatedAt;

    public UUID therapistProfileId() {
        return id == null ? null : id.getTherapistProfileId();
    }

    public UUID patientProfileId() {
        return id == null ? null : id.getPatientProfileId();
    }
}
