package com.mhsa.backend.auth.messaging;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mhsa.backend.auth.model.TherapistProfile;

/**
 * Flat snapshot of the therapist-profile fields that therapist-api mirrors. Shared by the
 * {@code therapist.profile.updated} event payload and the {@code /internal/therapist-profiles}
 * reconciliation endpoint so both expose exactly the same field set.
 *
 * <p>{@code profileId} is the therapist's profile id, which equals therapist-api's
 * {@code therapists.therapist_id}.
 */
public record TherapistProfileView(
        UUID profileId,
        String fullName,
        String avatarUrl,
        String gender,
        String specialization,
        String bio,
        Integer yearsExperience,
        List<String> languages,
        String licenseUrl,
        String licenseStatus) {

    public static TherapistProfileView from(TherapistProfile t) {
        return new TherapistProfileView(
                t.getId(),
                t.getFullName(),
                t.getAvatarUrl(),
                t.getGender(),
                t.getSpecialization(),
                t.getBio(),
                t.getYearsOfExperience(),
                t.getLanguages(),
                t.getLicenseDocumentUrl(),
                t.getLicenseStatus() == null ? null : t.getLicenseStatus().name());
    }

    /**
     * Renders the profile fields into a flat JSON object. Built via {@link ObjectMapper} (not the
     * entity binder) so free-text fields like {@code bio}/{@code fullName} are correctly escaped and
     * {@code languages} serializes as a JSON string array. {@code profileId} is emitted as a string;
     * {@code yearsExperience} as a JSON number; nulls are preserved.
     */
    public ObjectNode toJson(ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        // put(String, String) with a null value writes a JSON null, so no per-field null guards.
        node.put("profileId", profileId == null ? null : profileId.toString());
        node.put("fullName", fullName);
        node.put("avatarUrl", avatarUrl);
        node.put("gender", gender);
        node.put("specialization", specialization);
        node.put("bio", bio);
        if (yearsExperience == null) {
            node.putNull("yearsExperience");
        } else {
            node.put("yearsExperience", yearsExperience.intValue());
        }
        if (languages == null) {
            node.putNull("languages");
        } else {
            ArrayNode arr = node.putArray("languages");
            languages.forEach(arr::add);
        }
        node.put("licenseUrl", licenseUrl);
        node.put("licenseStatus", licenseStatus);
        return node;
    }
}
