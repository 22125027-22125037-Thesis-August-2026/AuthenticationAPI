package com.mhsa.backend.tracking.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "breathing_logs",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_breathing_logs_profile_entry_date", columnNames = {"profile_id", "entry_date"})
        },
        indexes = {
            @Index(name = "idx_breathing_logs_profile_entry_date", columnList = "profile_id, entry_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreathingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "breathing_log_id")
    private UUID id;

    // Keep profile reference as scalar UUID to preserve loose coupling.
    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Builder.Default
    @Column(name = "total_duration_seconds", nullable = false)
    private Integer totalDurationSeconds = 0;

    @Builder.Default
    @Column(name = "sessions_completed", nullable = false)
    private Integer sessionsCompleted = 0;

    @Builder.Default
    @Column(name = "goal_seconds", nullable = false)
    private Integer goalSeconds = 300;

    @Builder.Default
    @Column(name = "source", nullable = false, length = 50)
    private String source = "GUIDED_SESSION";

    @Column(name = "entry_date", nullable = false)
    @Builder.Default
    private LocalDate entryDate = LocalDate.now();

    @Column(name = "logged_at", nullable = false)
    @Builder.Default
    private LocalDateTime loggedAt = LocalDateTime.now();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
