package com.psiw.proj.backend.utils.responseDto;

import com.psiw.proj.backend.utils.responseDto.helpers.MovieSimpleDto;
import com.psiw.proj.backend.utils.responseDto.helpers.RoomDto;
import com.psiw.proj.backend.utils.responseDto.helpers.SeatDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Szczegółowe informacje o seansie filmowym")
@Builder
public record ScreeningDetailsResponse(
        @Schema(description = "ID seansu", example = "201", required = true)
        @NotNull Long id,

        @Schema(description = "Informacje o filmie", required = true)
        @NotNull MovieSimpleDto movie,

        @Schema(description = "Informacje o sali", required = true)
        @NotNull RoomDto room,

        @Schema(description = "Czas rozpoczęcia", example = "2025-05-23T20:00:00", required = true)
        @NotNull LocalDateTime startTime,

        @Schema(description = "Czas trwania w minutach", required = true)
        @NotNull Long duration,

        @Schema(description = "Lista miejsc", required = true)
        @NotNull List<SeatDto> seats

) {
}