package com.mhsa.backend.auth.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dob;
    private String role;
    private Integer creditsBalance; // Số dư ví
}
