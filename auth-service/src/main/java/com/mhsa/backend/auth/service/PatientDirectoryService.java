package com.mhsa.backend.auth.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mhsa.backend.auth.dto.PatientDetailResponse;
import com.mhsa.backend.auth.model.Profile;
import com.mhsa.backend.auth.model.TeenProfile;
import com.mhsa.backend.auth.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

/**
 * Read-only access to patient demographics for the therapist console.
 * Authorization (active grant / admin / self) is enforced at the controller
 * via {@code AccessGuard}; this service only assembles the payload.
 */
@Service
@RequiredArgsConstructor
public class PatientDirectoryService {

    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public PatientDetailResponse getPatientDetail(UUID profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        LocalDate dob = profile.getDateOfBirth();

        PatientDetailResponse.PatientDetailResponseBuilder builder = PatientDetailResponse.builder()
                .profileId(profile.getId())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .role(profile.getRole() != null ? profile.getRole().name() : null)
                .avatarUrl(profile.getAvatarUrl())
                .dateOfBirth(dob)
                .age(dob != null ? Period.between(dob, LocalDate.now()).getYears() : null)
                .gender(profile.getGender())
                .phoneNumber(profile.getPhoneNumber());

        if (profile instanceof TeenProfile teen) {
            builder.school(teen.getSchool())
                    .emergencyContact(teen.getEmergencyContact());
        }

        return builder.build();
    }
}
