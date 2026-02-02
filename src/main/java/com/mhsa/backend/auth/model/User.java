package com.mhsa.backend.auth.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data // Lombok sinh Getter/Setter/ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;

    // --- Thông tin đăng nhập (Chỉ dành cho Parent) ---
    @Column(unique = true) // Email không được trùng
    private String email;

    private String password; // Hash password

    // --- Thông tin cá nhân ---
    @Column(nullable = false)
    private String fullName;

    private LocalDate dob; // Ngày sinh

    private String phoneNumber;

    // --- Logic Gia đình (Parent - Child) ---
    @Enumerated(EnumType.STRING)
    private UserRole role; // MANAGER (Cha mẹ) hoặc DEPENDENT (Con)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private User parent; // Nếu là con, trường này sẽ trỏ về ID của cha mẹ

    @Column(length = 4)
    private String pinCode; // Mã PIN 4 số (Chỉ dành cho con đăng nhập)

    // --- Ví tiền (Quản lý chung bởi Parent) ---
    @Builder.Default
    private Integer creditsBalance = 0;

    // --- Audit ---
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
