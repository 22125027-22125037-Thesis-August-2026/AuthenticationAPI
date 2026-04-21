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
        name = "food_logs",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_food_logs_profile_entry_date", columnNames = {"profile_id", "entry_date"})
        },
        indexes = {
            @Index(name = "idx_food_logs_profile_entry_date", columnList = "profile_id, entry_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "food_id")
    private UUID id;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Builder.Default
    @Column(name = "water_glasses", nullable = false)
    private Integer waterGlasses = 0;

    @Column(name = "food_description", nullable = false, columnDefinition = "TEXT")
    private String foodDescription;

    @Column(name = "satiety_level", nullable = false, length = 100)
    private String satietyLevel;

    @Column(name = "entry_date", nullable = false)
    @Builder.Default
    private LocalDate entryDate = LocalDate.now();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
