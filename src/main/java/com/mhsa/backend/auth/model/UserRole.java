package com.mhsa.backend.auth.model;

public enum UserRole {
    MANAGER, // Tài khoản cha mẹ (Có email, pass, ví tiền)
    DEPENDENT, // Tài khoản con (Chỉ có PIN, phụ thuộc cha mẹ)
    DOCTOR, // Tài khoản bác sĩ
    ADMIN       // Quản trị viên
}
