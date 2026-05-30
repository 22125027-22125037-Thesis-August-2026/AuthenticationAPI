package com.mhsa.backend.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dob;
    private String role;
    private Integer creditsBalance;
    private String avatarUrl;

    // --- Therapist-only fields (null for other roles) ---
    private String specialization;
    private String bio;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private List<String> languages;
    private String licenseNumber;
    private String licenseAuthority;
    private LocalDate licenseExpiresAt;
    private String licenseStatus;
}
