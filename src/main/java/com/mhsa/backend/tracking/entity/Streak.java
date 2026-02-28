package com.mhsa.backend.tracking.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "streaks",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_streak_profile_type", columnNames = {"profile_id", "streak_type"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Streak {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "streak_id")
    private UUID id;

    // Keep profile reference as scalar UUID to preserve loose coupling.
    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(name = "streak_type", nullable = false, length = 50)
    private String streakType;

    @Builder.Default
    @Column(name = "current_count", nullable = false)
    private Integer currentCount = 0;

    @Builder.Default
    @Column(name = "longest_count", nullable = false)
    private Integer longestCount = 0;

    @Column(name = "last_logged_at")
    private LocalDateTime lastLoggedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
