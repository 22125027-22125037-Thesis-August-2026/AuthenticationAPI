package com.mhsa.backend.auth.dto;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;

import com.mhsa.backend.auth.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String fullName;
    private String avatarUrl;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
    private String phoneNumber;
    private LocalDate dob;

    @NotNull
    private Role role;

    // Teen profile fields
    private String school;
    private String emergencyContact;

    // Therapist profile fields
    private String specialization;
    private String bio;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private Boolean verified;

    // Parent profile fields
    private UUID linkedTeenId;
}
