package com.mhsa.backend.auth.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mhsa.backend.auth.dto.AuthResponse;
import com.mhsa.backend.auth.dto.LoginRequest;
import com.mhsa.backend.auth.dto.RegisterRequest;
import com.mhsa.backend.auth.dto.UserResponse;
import com.mhsa.backend.auth.model.ParentProfile;
import com.mhsa.backend.auth.model.Profile;
import com.mhsa.backend.auth.model.Role;
import com.mhsa.backend.auth.model.User;
import com.mhsa.backend.auth.model.TeenProfile;
import com.mhsa.backend.auth.model.TherapistProfile;
import com.mhsa.backend.auth.repository.ProfileRepository;
import com.mhsa.backend.auth.repository.UserRepository;
import com.mhsa.backend.auth.utils.JwtUtils;
import com.mhsa.backend.auth.security.AuthenticatedUserPrincipal;

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
        // 1. Check email trùng
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        if (request.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        // 2. Tạo User mới
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Mã hóa pass
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDob(request.getDob());
        user.setRole(request.getRole());

        userRepository.save(user);
        Profile profile = buildProfile(user, request);
        profileRepository.save(profile);
        user.setProfile(profile);
        return "User registered successfully!";
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Xác thực username/password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Tìm user để lấy thông tin
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        Profile profile = resolveOrCreateProfile(user);

        // 3. Tạo Token
        var token = jwtUtils.generateToken(user.getId(), profile.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(token, profile.getId(), user.getEmail(), user.getRole().name());
    }

    public UserResponse getCurrentUser() {
        // 1. Lấy userId từ Security Context (Do JwtFilter đã set vào trước đó)
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

        // 3. Convert sang DTO (Không trả về password!)
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dob(user.getDob())
                .role(user.getRole().name())
                .creditsBalance(user.getCreditsBalance())
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

        if (role == Role.PARENT) {
            ParentProfile profile = new ParentProfile();
            populateBaseProfile(profile, user, request);
            profile.setLinkedTeenId(request.getLinkedTeenId());
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
                        case PARENT -> {
                            ParentProfile parentProfile = new ParentProfile();
                            populateBaseProfile(parentProfile, user, null);
                            yield parentProfile;
                        }
                        case ADMIN -> {
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
    }
}
