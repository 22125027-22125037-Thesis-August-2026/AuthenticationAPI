package com.mhsa.backend.auth.model;

/**
 * Lifecycle of a therapist&lt;-&gt;patient assignment as mirrored in the local read-model.
 * Mapped straight through from therapist-api's event/snapshot {@code status}.
 */
public enum AssignmentStatus {
    ACTIVE,
    INACTIVE
}
