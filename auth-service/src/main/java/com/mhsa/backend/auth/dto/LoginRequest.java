package com.mhsa.backend.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String email;
    private String password;

    /** Optional client/device label persisted with the issued refresh token (for a devices list). */
    private String deviceLabel;
}
