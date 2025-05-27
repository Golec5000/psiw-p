package com.psiw.proj.backend.utils.responseDto.helpers;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(name = "ScreeningSummaryDto", description = "Podsumowanie informacji o seansie")
public record ScreeningSummaryDto(

        @Schema(description = "Unikalny identyfikator seansu", example = "42")
        Long id,

        @Schema(description = "Data i godzina rozpoczęcia seansu", example = "2025-05-27T19:30:00")
        LocalDateTime startTime,

        @Schema(description = "Czas trwania seansu w minutach", example = "120")
        Long duration

) {
}
