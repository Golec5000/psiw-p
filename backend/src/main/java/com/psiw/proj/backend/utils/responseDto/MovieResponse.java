package com.psiw.proj.backend.utils.responseDto;

import com.psiw.proj.backend.utils.responseDto.helpers.ScreeningSummaryDto;
import lombok.Builder;

import java.util.List;

@Builder
public record MovieResponse(
        Long id,
        String title,
        String description,
        String image,
        List<ScreeningSummaryDto> screenings
) {}
