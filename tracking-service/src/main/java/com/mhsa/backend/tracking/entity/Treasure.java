package com.mhsa.backend.tracking.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single treasure in the user's "Hộp Trân Quý" (Treasure Box) — a personal comfort
 * collection. Each treasure is a short note in one of the 8 mobile categories, optionally
 * anchored by a single inline media item (image/audio/video) kept in object storage.
 * Only the object key is persisted; responses carry a freshly signed media URL.
 */
@Entity
@Table(name = "treasures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Treasure {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "treasure_id")
    private UUID id;

    // Keep profile reference as scalar UUID to preserve loose coupling.
    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    // One of the 8 mobile category ids (reasons, joy, people, dreams, moments,
    // affirmations, wins, comfort). Stored as the raw string for forward-compatibility.
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "emoji", length = 32)
    private String emoji;

    // Object-storage key of the attached media; null when the treasure is text-only.
    @Column(name = "media_object_key", length = 1000)
    private String mediaObjectKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", length = 20)
    private MediaAttachment.MediaType mediaType;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
