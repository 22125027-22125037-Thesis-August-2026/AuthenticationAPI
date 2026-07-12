package com.mhsa.backend.auth.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mhsa.backend.auth.messaging.TherapistProfileView;
import com.mhsa.backend.auth.model.TherapistProfile;
import com.mhsa.backend.auth.repository.TherapistProfileRepository;

import lombok.RequiredArgsConstructor;

/**
 * Internal reconciliation endpoint: a full, paginated snapshot of every therapist profile with the
 * same fields carried by the {@code therapist.profile.updated} event. Lives under {@code /internal/}
 * (blocked at nginx, so not reachable through the public gateway) and is consumed by therapist-api
 * to heal any missed events.
 */
@RestController
@RequestMapping("/internal/therapist-profiles")
@RequiredArgsConstructor
public class InternalTherapistProfileController {

    private static final int MAX_PAGE_SIZE = 500;

    private final TherapistProfileRepository therapistProfileRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ObjectNode> listTherapistProfiles(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        // Sort by id for a stable, deterministic ordering across pages.
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "id"));

        Page<TherapistProfile> result = therapistProfileRepository.findAll(pageable);

        ArrayNode content = objectMapper.createArrayNode();
        for (TherapistProfile therapist : result.getContent()) {
            content.add(TherapistProfileView.from(therapist).toJson(objectMapper));
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.set("content", content);
        body.put("page", result.getNumber());
        body.put("size", result.getSize());
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages", result.getTotalPages());
        body.put("last", result.isLast());

        return ResponseEntity.ok(body);
    }
}
