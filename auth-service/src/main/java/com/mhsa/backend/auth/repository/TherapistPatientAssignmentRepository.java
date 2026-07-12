package com.mhsa.backend.auth.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mhsa.backend.auth.model.AssignmentStatus;
import com.mhsa.backend.auth.model.TherapistPatientAssignment;
import com.mhsa.backend.auth.model.TherapistPatientAssignmentId;

public interface TherapistPatientAssignmentRepository
        extends JpaRepository<TherapistPatientAssignment, TherapistPatientAssignmentId> {

    /**
     * Authorization check: does an ACTIVE assignment exist linking this therapist to this patient?
     * Drives {@code AccessGuard.canViewPatientProfile}.
     */
    boolean existsByIdTherapistProfileIdAndIdPatientProfileIdAndStatus(
            UUID therapistProfileId, UUID patientProfileId, AssignmentStatus status);

    /** Used by reconciliation to find locally-ACTIVE rows that may need deactivating. */
    List<TherapistPatientAssignment> findByStatus(AssignmentStatus status);
}
