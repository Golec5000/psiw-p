package com.psiw.proj.backend.utils.responseDto.helpers;

import lombok.Builder;

import java.time.Duration;
import java.time.LocalDateTime;

@Builder
public record ScreeningSummaryDto(
        Long id,
        LocalDateTime startTime,
        Duration duration
) {}
