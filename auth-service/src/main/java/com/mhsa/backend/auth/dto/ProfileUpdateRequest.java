package com.mhsa.backend.auth.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String avatarUrl;
    private String phoneNumber;

    // --- Therapist-only fields; ignored for other roles ---
    private String specialization;
    private String bio;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private List<String> languages;
}
