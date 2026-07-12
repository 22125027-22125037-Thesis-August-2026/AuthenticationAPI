package com.mhsa.backend.tracking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreasureRequest {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "Treasure category id (one of the 8 mobile categories)", example = "people")
    private String category;

    @NotBlank
    @Schema(description = "Short note the user wants to remember", example = "Mẹ luôn để dành phần cơm và đợi mình về.")
    private String content;

    @Size(max = 32)
    @Schema(description = "Emoji that anchors this treasure", example = "❤️")
    private String emoji;
}
