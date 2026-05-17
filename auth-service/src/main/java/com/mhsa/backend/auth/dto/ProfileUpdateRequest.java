package com.mhsa.backend.auth.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String avatarUrl;
    private String phoneNumber;
}
