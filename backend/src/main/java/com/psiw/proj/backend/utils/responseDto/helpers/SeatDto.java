package com.psiw.proj.backend.utils.responseDto.helpers;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Schema(description = "Informacje o miejscu w sali kinowej")
@Builder
public record SeatDto(
        @Schema(description = "ID miejsca", example = "501")
        @NotNull Long id,

        @Schema(description = "Numer rzędu", example = "5")
        int rowNumber,

        @Schema(description = "Numer kolumny", example = "8")
        int columnNumber,

        @Schema(description = "Numer miejsca", example = "12")
        int seatNumber,

        @Schema(description = "Czy miejsce jest dostępne", example = "true")
        boolean available
) {
}

