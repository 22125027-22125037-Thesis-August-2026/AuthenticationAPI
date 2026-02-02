package com.mhsa.backend.auth.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class RegisterRequest {

    private String fullName;
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDate dob;
}
