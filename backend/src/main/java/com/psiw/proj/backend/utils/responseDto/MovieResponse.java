package com.psiw.proj.backend.utils.responseDto;

import com.psiw.proj.backend.utils.responseDto.helpers.ScreeningSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "Film dostępny w repertuarze z listą seansów")
@Builder
public record MovieResponse(
        @Schema(description = "ID filmu", example = "101")
        Long id,

        @Schema(description = "Tytuł filmu", example = "Incepcja")
        String title,

        @Schema(description = "Opis filmu", example = "Film science-fiction w reżyserii Christophera Nolana")
        String description,

        @Schema(description = "URL do plakatu", example = "https://example.com/poster.jpg")
        String image,

        @Schema(description = "Lista seansów dla filmu") List<ScreeningSummaryDto>
        screenings
) {
}