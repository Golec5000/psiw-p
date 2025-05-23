package com.psiw.proj.backend.utils.responseDto.helpers;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Podstawowe informacje o filmie")
@Builder
public record MovieSimpleDto(
        @Schema(description = "ID filmu", example = "101")
        Long id,

        @Schema(description = "Tytuł filmu", example = "Matrix")
        String title
) {
}