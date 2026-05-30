package com.mhsa.backend.auth.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mhsa.backend.auth.dto.LicenseResponse;
import com.mhsa.backend.auth.model.LicenseStatus;
import com.mhsa.backend.auth.model.Profile;
import com.mhsa.backend.auth.model.TherapistProfile;
import com.mhsa.backend.auth.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

/**
 * Owns the therapist license-verification lifecycle: reading current state,
 * submitting renewal documents, and the admin-side verify/reject transitions.
 */
@Service
@RequiredArgsConstructor
public class TherapistLicenseService {

    private final ProfileRepository profileRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public LicenseResponse getLicense(UUID profileId) {
        return toResponse(loadTherapistProfile(profileId));
    }

    /**
     * Submits (or re-submits) a license document and metadata. Resets the profile to
     * {@code PENDING_VERIFICATION} so an admin re-reviews it.
     */
    @Transactional
    public LicenseResponse renewLicense(UUID profileId,
                                        MultipartFile document,
                                        String licenseNumber,
                                        String licenseAuthority,
                                        LocalDate licenseExpiresAt) {
        TherapistProfile therapist = loadTherapistProfile(profileId);

        if (document != null && !document.isEmpty()) {
            String url = fileStorageService.storeLicenseDocument(document, profileId);
            therapist.setLicenseDocumentUrl(url);
        }
        if (licenseNumber != null && !licenseNumber.isBlank()) {
            therapist.setLicenseNumber(licenseNumber.trim());
        }
        if (licenseAuthority != null && !licenseAuthority.isBlank()) {
            therapist.setLicenseAuthority(licenseAuthority.trim());
        }
        if (licenseExpiresAt != null) {
            therapist.setLicenseExpiresAt(licenseExpiresAt);
        }

        therapist.setLicenseStatus(LicenseStatus.PENDING_VERIFICATION);
        therapist.setIsVerified(false);
        profileRepository.save(therapist);

        return toResponse(therapist);
    }

    @Transactional
    public LicenseResponse verifyLicense(UUID profileId) {
        TherapistProfile therapist = loadTherapistProfile(profileId);
        therapist.setLicenseStatus(LicenseStatus.VERIFIED);
        therapist.setIsVerified(true);
        profileRepository.save(therapist);
        return toResponse(therapist);
    }

    @Transactional
    public LicenseResponse rejectLicense(UUID profileId) {
        TherapistProfile therapist = loadTherapistProfile(profileId);
        therapist.setLicenseStatus(LicenseStatus.REJECTED);
        therapist.setIsVerified(false);
        profileRepository.save(therapist);
        return toResponse(therapist);
    }

    private TherapistProfile loadTherapistProfile(UUID profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        if (!(profile instanceof TherapistProfile therapist)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile is not a therapist");
        }
        return therapist;
    }

    private LicenseResponse toResponse(TherapistProfile therapist) {
        return LicenseResponse.builder()
                .profileId(therapist.getId())
                .status(therapist.getLicenseStatus() != null ? therapist.getLicenseStatus().name() : null)
                .licenseNumber(therapist.getLicenseNumber())
                .licenseAuthority(therapist.getLicenseAuthority())
                .licenseExpiresAt(therapist.getLicenseExpiresAt())
                .documentUrl(therapist.getLicenseDocumentUrl())
                .verified(Boolean.TRUE.equals(therapist.getIsVerified()))
                .build();
    }
}
