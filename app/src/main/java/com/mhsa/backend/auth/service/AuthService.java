package com.mhsa.backend.auth.service;

import java.time.LocalDateTime;
import java.util.UUID;

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
import com.mhsa.backend.auth.model.Profile;
import com.mhsa.backend.auth.jwt.Role;
import com.mhsa.backend.auth.model.User;
import com.mhsa.backend.auth.model.TeenProfile;
import com.mhsa.backend.auth.model.TherapistProfile;
import com.mhsa.backend.auth.repository.ProfileRepository;
import com.mhsa.backend.auth.repository.UserRepository;
import com.mhsa.backend.auth.jwt.JwtUtils;
import com.mhsa.backend.auth.jwt.AuthenticatedUserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public String register(RegisterRequest request) {
        // 1. Check email trÃ¹ng
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        if (request.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        // 2. Táº¡o User má»›i
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // MÃ£ hÃ³a pass
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDob(request.getDob());
        user.setRole(request.getRole());
        user.setPinCode(request.getPinCode());
        user.setAccountType(request.getAccountType());

        userRepository.save(user);
        Profile profile = buildProfile(user, request);
        profileRepository.save(profile);
        user.setProfile(profile);
        return "User registered successfully!";
    }

    public AuthResponse login(LoginRequest request) {
        // 1. XÃ¡c thá»±c username/password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. TÃ¬m user Ä‘á»ƒ láº¥y thÃ´ng tin
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        Profile profile = resolveOrCreateProfile(user);

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // 3. Táº¡o Token
        var token = jwtUtils.generateToken(user.getId(), profile.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(token, profile.getId(), user.getEmail(), user.getRole().name());
    }

    public UserResponse getCurrentUser() {
        // 1. Láº¥y userId tá»« Security Context (Do JwtFilter Ä‘Ã£ set vÃ o trÆ°á»›c Ä‘Ã³)
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Unauthorized");
        }

        UUID currentUserId;
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof AuthenticatedUserPrincipal authenticatedUserPrincipal) {
                currentUserId = authenticatedUserPrincipal.userId();
            } else {
                currentUserId = UUID.fromString(authentication.getName());
            }
        } catch (IllegalArgumentException e) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Unauthorized");
        }

        // 2. Query DB
        var user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var profile = profileRepository.findByUser_Id(currentUserId).orElse(null);

        // 3. Convert sang DTO (KhÃ´ng tráº£ vá» password!)
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dob(user.getDob())
                .role(user.getRole().name())
                .creditsBalance(user.getCreditsBalance())
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .build();
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var profile = profileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        boolean userDirty = false;

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
            profile.setFullName(request.getFullName());
            userDirty = true;
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
            profile.setPhoneNumber(request.getPhoneNumber());
            userDirty = true;
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (userDirty) {
            userRepository.save(user);
        }
        profileRepository.save(profile);

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dob(user.getDob())
                .role(user.getRole().name())
                .creditsBalance(user.getCreditsBalance())
                .avatarUrl(profile.getAvatarUrl())
                .build();
    }

    private Profile buildProfile(User user, RegisterRequest request) {
        Role role = request.getRole();

        if (role == Role.TEEN) {
            TeenProfile profile = new TeenProfile();
            populateBaseProfile(profile, user, request);
            profile.setSchool(request.getSchool());
            profile.setEmergencyContact(request.getEmergencyContact());
            return profile;
        }

        if (role == Role.THERAPIST) {
            TherapistProfile profile = new TherapistProfile();
            populateBaseProfile(profile, user, request);
            profile.setSpecialization(request.getSpecialization());
            profile.setBio(request.getBio());
            profile.setYearsOfExperience(request.getYearsOfExperience());
            profile.setConsultationFee(request.getConsultationFee());
            profile.setIsVerified(Boolean.TRUE.equals(request.getVerified()));
            return profile;
        }

        Profile profile = new Profile();
        populateBaseProfile(profile, user, request);
        return profile;
    }

    private Profile resolveOrCreateProfile(User user) {
        return profileRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    Profile profile = switch (user.getRole()) {
                        case TEEN -> {
                            TeenProfile teenProfile = new TeenProfile();
                            populateBaseProfile(teenProfile, user, null);
                            yield teenProfile;
                        }
                        case THERAPIST -> {
                            TherapistProfile therapistProfile = new TherapistProfile();
                            populateBaseProfile(therapistProfile, user, null);
                            yield therapistProfile;
                        }
                        case PARENT, ADMIN -> {
                            Profile baseProfile = new Profile();
                            populateBaseProfile(baseProfile, user, null);
                            yield baseProfile;
                        }
                    };
                    return profileRepository.save(profile);
                });
    }

    private void populateBaseProfile(Profile profile, User user, RegisterRequest request) {
        profile.setUser(user);
        profile.setFullName(request == null ? user.getFullName() : request.getFullName());
        profile.setAvatarUrl(request == null ? null : request.getAvatarUrl());
        profile.setDateOfBirth(request == null ? user.getDob() : request.getDob());
        profile.setPhoneNumber(request == null ? user.getPhoneNumber() : request.getPhoneNumber());
        if (request != null) {
            profile.setGender(request.getGender());
        }
    }
}

