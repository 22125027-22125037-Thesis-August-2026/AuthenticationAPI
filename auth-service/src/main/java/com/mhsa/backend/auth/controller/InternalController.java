package com.mhsa.backend.auth.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mhsa.backend.auth.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/internal/v1")
@RequiredArgsConstructor
@Slf4j
public class InternalController {

    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/profile/{profileId}/summary")
    public ResponseEntity<ObjectNode> getProfileSummary(@PathVariable UUID profileId) {
        return profileRepository.findById(profileId)
                .map(profile -> {
                    ObjectNode summary = objectMapper.createObjectNode();
                    summary.put("profileId", profileId.toString());
                    summary.put("name", profile.getFullName() != null ? profile.getFullName() : "");
                    summary.put("email", profile.getUser() != null && profile.getUser().getEmail() != null ? profile.getUser().getEmail() : "");
                    summary.put("role", profile.getUser() != null && profile.getUser().getRole() != null ? profile.getUser().getRole().toString() : "USER");
                    summary.put("avatarUrl", profile.getAvatarUrl() != null ? profile.getAvatarUrl() : "");
                    return ResponseEntity.ok(summary);
                })
                .orElseGet(() -> {
                    log.warn("Profile not found: profileId={}", profileId);
                    return ResponseEntity.notFound().build();
                });
    }
}
