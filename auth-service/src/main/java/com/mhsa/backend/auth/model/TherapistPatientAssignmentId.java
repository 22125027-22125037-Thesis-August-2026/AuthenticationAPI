package com.mhsa.backend.auth.model;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite key for {@link TherapistPatientAssignment}: a single assignment is uniquely
 * identified by the (therapist, patient) profile pair.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TherapistPatientAssignmentId implements Serializable {

    @Column(name = "therapist_profile_id", nullable = false)
    private UUID therapistProfileId;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;
}
