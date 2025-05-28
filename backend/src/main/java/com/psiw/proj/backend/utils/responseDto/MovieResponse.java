package com.psiw.proj.backend.utils.responseDto;

import com.psiw.proj.backend.utils.responseDto.helpers.ScreeningSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Schema(description = "Film dostępny w repertuarze z listą seansów")
@Builder
public record MovieResponse(
        @Schema(description = "ID filmu", example = "101", required = true)
        @NotNull Long id,

        @Schema(description = "Tytuł filmu", example = "Incepcja", required = true)
        @NotNull String title,

        @Schema(description = "Opis filmu", example = "Film science-fiction w reżyserii Christophera Nolana", required = true)
        @NotNull String description,

        @Schema(description = "Lista seansów dla filmu", required = true)
        @NotNull List<ScreeningSummaryDto> screenings
) {
}