package com.mhsa.backend.auth.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
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

    @Column(nullable = false)
    @Convert(converter = RoleAttributeConverter.class)
    private Role role;

    // --- Legacy / transitional fields kept during migration ---
    private String fullName;

    private LocalDate dob; // Ngày sinh

    private String phoneNumber;

    // --- Ví tiền (Quản lý chung bởi Parent) ---
    @Builder.Default
    private Integer creditsBalance = 0;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    // --- Audit ---
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
