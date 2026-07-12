package com.mhsa.backend.auth.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("THERAPIST")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TherapistProfile extends Profile {

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "consultation_fee", precision = 12, scale = 2)
    private BigDecimal consultationFee;

    @lombok.Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "license_authority")
    private String licenseAuthority;

    @Column(name = "license_expires_at")
    private LocalDate licenseExpiresAt;

    @Column(name = "license_document_url", length = 1024)
    private String licenseDocumentUrl;

    @lombok.Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "license_status", nullable = false, length = 40)
    private LicenseStatus licenseStatus = LicenseStatus.PENDING_VERIFICATION;

    @Convert(converter = StringListConverter.class)
    @Column(name = "languages", length = 512)
    private List<String> languages;
}
