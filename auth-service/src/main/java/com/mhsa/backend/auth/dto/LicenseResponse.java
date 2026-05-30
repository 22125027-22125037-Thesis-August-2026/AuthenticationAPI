package com.mhsa.backend.auth.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LicenseResponse {
    private UUID profileId;
    private String status;
    private String licenseNumber;
    private String licenseAuthority;
    private LocalDate licenseExpiresAt;
    private String documentUrl;
    private boolean verified;
}
