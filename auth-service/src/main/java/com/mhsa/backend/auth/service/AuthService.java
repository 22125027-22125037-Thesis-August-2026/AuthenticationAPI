package com.mhsa.backend.auth.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mhsa.backend.auth.dto.AuthResponse;
import com.mhsa.backend.auth.dto.LoginRequest;
import com.mhsa.backend.auth.dto.ProfileUpdateRequest;
import com.mhsa.backend.auth.dto.RegisterRequest;
import com.mhsa.backend.auth.dto.UserResponse;
import com.mhsa.backend.auth.messaging.TherapistProfileChangedEvent;
import com.mhsa.backend.auth.model.Profile;
import com.mhsa.backend.auth.jwt.Role;
import com.mhsa.backend.auth.model.TeenProfile;
import com.mhsa.backend.auth.model.TherapistProfile;
import com.mhsa.backend.auth.repository.ProfileRepository;
import com.mhsa.backend.auth.jwt.JwtUtils;
import com.mhsa.backend.auth.jwt.AuthenticatedUserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;
    private final RefreshTokenService refreshTokenService;

    /** Access-token lifetime in ms; surfaced to clients as {@code expiresIn} (seconds). */
    @Value("${mhsa.app.jwtExpirationMs}")
    private long accessExpirationMs;

    @Transactional
    public String register(RegisterRequest request) {
        // 1. Check email trÃ¹ng
        if (profileRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        if (request.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        // 2. Táº¡o Profile má»›i (the single account entity since the users/profiles merge)
        Profile profile = buildProfile(request);
        profileRepository.save(profile);
        return "User registered successfully!";
    }

    public AuthResponse login(LoginRequest request) {
        // 1. XÃ¡c thá»±c username/password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. TÃ¬m profile Ä‘á»ƒ láº¥y thÃ´ng tin
        var profile = profileRepository.findByEmail(request.getEmail())
                .orElseThrow();

        profile.setLastLogin(LocalDateTime.now());
        profileRepository.save(profile);

        // 3. Issue a short-lived access token plus a long-lived rotating refresh token.
        String accessToken = jwtUtils.generateToken(profile.getId(), profile.getEmail(), profile.getRole());
        var refresh = refreshTokenService.issue(profile, request.getDeviceLabel());

        return buildAuthResponse(accessToken, refresh.rawToken(), profile);
    }

    /**
     * Exchanges a valid refresh token for a fresh access token and a rotated refresh token.
     * Delegates validation/rotation (including reuse detection) to {@link RefreshTokenService}.
     */
    @Transactional
    public AuthResponse refresh(String refreshToken, String deviceLabel) {
        var rotation = refreshTokenService.rotate(refreshToken, deviceLabel);
        Profile profile = rotation.profile();

        String accessToken = jwtUtils.generateToken(profile.getId(), profile.getEmail(), profile.getRole());
        return buildAuthResponse(accessToken, rotation.rawToken(), profile);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, Profile profile) {
        return AuthResponse.builder()
                .token(accessToken) // legacy alias, kept during client rollout
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessExpirationMs / 1000)
                .profileId(profile.getId())
                .email(profile.getEmail())
                .role(profile.getRole().name())
                .build();
    }

    public UserResponse getCurrentUser() {
        // 1. Láº¥y profileId tá»« Security Context (Do JwtFilter Ä‘Ã£ set vÃ o trÆ°á»›c Ä‘Ã³)
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Unauthorized");
        }

        UUID currentProfileId;
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof AuthenticatedUserPrincipal authenticatedUserPrincipal) {
                currentProfileId = authenticatedUserPrincipal.profileId();
            } else {
                currentProfileId = UUID.fromString(authentication.getName());
            }
        } catch (IllegalArgumentException e) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Unauthorized");
        }

        // 2. Query DB
        var profile = profileRepository.findById(currentProfileId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Convert sang DTO (KhÃ´ng tráº£ vá» password!)
        return toUserResponse(profile);
    }

    @Transactional
    public UserResponse updateProfile(UUID profileId, ProfileUpdateRequest request) {
        var profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        // Therapist-specific fields are only applied when the profile is a therapist.
        if (profile instanceof TherapistProfile therapistProfile) {
            if (request.getSpecialization() != null) {
                therapistProfile.setSpecialization(request.getSpecialization());
            }
            if (request.getBio() != null) {
                therapistProfile.setBio(request.getBio());
            }
            if (request.getYearsOfExperience() != null) {
                therapistProfile.setYearsOfExperience(request.getYearsOfExperience());
            }
            if (request.getConsultationFee() != null) {
                therapistProfile.setConsultationFee(request.getConsultationFee());
            }
            if (request.getLanguages() != null) {
                therapistProfile.setLanguages(request.getLanguages());
            }
        }

        profileRepository.save(profile);

        // Fan out therapist changes so therapist-api can mirror them; emitted only AFTER_COMMIT.
        if (profile instanceof TherapistProfile therapistProfile) {
            eventPublisher.publishEvent(TherapistProfileChangedEvent.of(therapistProfile, Instant.now()));
        }

        return toUserResponse(profile);
    }

    /**
     * Builds the {@link UserResponse} for a profile, enriching it with therapist-specific
     * fields (specialization, license, languages, …) when the profile is a therapist.
     */
    private UserResponse toUserResponse(Profile profile) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .phoneNumber(profile.getPhoneNumber())
                .dob(profile.getDateOfBirth())
                .role(profile.getRole().name())
                .creditsBalance(profile.getCreditsBalance())
                .avatarUrl(profile.getAvatarUrl());

        if (profile instanceof TherapistProfile therapist) {
            builder.specialization(therapist.getSpecialization())
                    .bio(therapist.getBio())
                    .yearsOfExperience(therapist.getYearsOfExperience())
                    .consultationFee(therapist.getConsultationFee())
                    .languages(therapist.getLanguages())
                    .licenseNumber(therapist.getLicenseNumber())
                    .licenseAuthority(therapist.getLicenseAuthority())
                    .licenseExpiresAt(therapist.getLicenseExpiresAt())
                    .licenseStatus(therapist.getLicenseStatus() != null
                            ? therapist.getLicenseStatus().name()
                            : null);
        }

        return builder.build();
    }

    /**
     * Verifies the supplied current password and replaces it with the new one.
     * Throws {@link IllegalArgumentException} if the current password does not match
     * so the controller can map it to a 400 response.
     */
    @Transactional
    public void changePassword(UUID profileId, String currentPassword, String newPassword) {
        var profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (profile.getPassword() == null || !passwordEncoder.matches(currentPassword, profile.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
        if (passwordEncoder.matches(newPassword, profile.getPassword())) {
            throw new IllegalArgumentException("New password must differ from the current password");
        }

        profile.setPassword(passwordEncoder.encode(newPassword));
        profileRepository.save(profile);
    }

    private Profile buildProfile(RegisterRequest request) {
        Role role = request.getRole();

        if (role == Role.TEEN) {
            TeenProfile profile = new TeenProfile();
            populateBaseProfile(profile, request);
            profile.setSchool(request.getSchool());
            profile.setEmergencyContact(request.getEmergencyContact());
            return profile;
        }

        if (role == Role.THERAPIST) {
            TherapistProfile profile = new TherapistProfile();
            populateBaseProfile(profile, request);
            profile.setSpecialization(request.getSpecialization());
            profile.setBio(request.getBio());
            profile.setYearsOfExperience(request.getYearsOfExperience());
            profile.setConsultationFee(request.getConsultationFee());
            boolean verified = Boolean.TRUE.equals(request.getVerified());
            profile.setIsVerified(verified);
            profile.setLicenseStatus(verified
                    ? com.mhsa.backend.auth.model.LicenseStatus.VERIFIED
                    : com.mhsa.backend.auth.model.LicenseStatus.PENDING_VERIFICATION);
            return profile;
        }

        Profile profile = new Profile();
        populateBaseProfile(profile, request);
        return profile;
    }

    private void populateBaseProfile(Profile profile, RegisterRequest request) {
        profile.setEmail(request.getEmail());
        profile.setPassword(passwordEncoder.encode(request.getPassword())); // MÃ£ hÃ³a pass
        profile.setRole(request.getRole());
        profile.setPinCode(request.getPinCode());
        profile.setAccountType(request.getAccountType());
        profile.setFullName(request.getFullName());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setDateOfBirth(request.getDob());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setGender(request.getGender());
    }
}
