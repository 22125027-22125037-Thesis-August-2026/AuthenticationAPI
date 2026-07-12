package com.mhsa.backend.auth.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

/**
 * Therapist-facing view of a patient profile. Returned by
 * {@code GET /api/v1/patients/{profileId}} and authorized via an active data-access grant
 * (or ADMIN / self). Carries the demographics the therapist console needs.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientDetailResponse {
    private UUID profileId;
    private String fullName;
    private String email;
    private String role;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private Integer age;
    private String gender;
    private String phoneNumber;

    // Teen-profile fields (null for other roles)
    private String school;
    private String emergencyContact;
}
