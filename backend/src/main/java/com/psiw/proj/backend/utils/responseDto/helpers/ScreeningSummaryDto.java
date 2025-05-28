package com.psiw.proj.backend.utils.responseDto.helpers;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Duration;
import java.time.LocalDateTime;

@Builder
public record ScreeningSummaryDto(
        @Schema(description = "ID", required = true)
        Long id,
        @Schema(description = "Czas rozpoczecia", required = true)
        LocalDateTime startTime,
        @Schema(description = "Czas trwania", required = true)
        Long duration
) {}
