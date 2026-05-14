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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "sleep_logs",
        indexes = {
            @Index(name = "idx_sleep_logs_profile_entry_date", columnList = "profile_id, entry_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SleepLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sleep_log_id")
    private UUID id;

    // Keep profile reference as scalar UUID to preserve loose coupling.
    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(name = "sleep_start_at")
    private LocalDateTime sleepStartAt;

    @Column(name = "sleep_end_at")
    private LocalDateTime sleepEndAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "sleep_quality")
    private Integer sleepQuality;

    @Column(columnDefinition = "TEXT")
    private String note;

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
