package com.mhsa.backend.tracking.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryEntryResponse {

    @Schema(description = "Diary entry identifier", example = "a3f94374-03b0-4b7e-a218-53a3ad6dcc1f")
    private UUID id;

    @Schema(description = "Decrypted diary content shown to user", example = "Today was a productive day, I felt really focused.")
    private String content;

    @Schema(description = "Diary title", example = "A Productive Monday")
    private String title;

    @Schema(description = "Mood tag of the diary entry", example = "MOTIVATED")
    private String moodTag;

    @Schema(description = "Daily positivity score from 1 to 10", example = "8")
    private Integer positivityScore;

    @Schema(description = "Diary entry date in ISO-8601", example = "2026-04-10")
    private LocalDate entryDate;

    @Schema(description = "Attached media list")
    private List<MediaAttachmentResponse> attachments;

    @Schema(description = "Creation timestamp in ISO-8601", example = "2026-02-28T21:12:20")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp in ISO-8601", example = "2026-02-28T21:20:00")
    private LocalDateTime updatedAt;
}
