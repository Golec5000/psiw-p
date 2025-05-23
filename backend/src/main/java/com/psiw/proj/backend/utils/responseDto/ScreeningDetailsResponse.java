package com.psiw.proj.backend.utils.responseDto;

import com.psiw.proj.backend.utils.responseDto.helpers.MovieSimpleDto;
import com.psiw.proj.backend.utils.responseDto.helpers.RoomDto;
import com.psiw.proj.backend.utils.responseDto.helpers.SeatDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Szczegółowe informacje o seansie filmowym")
@Builder
public record ScreeningDetailsResponse(
        @Schema(description = "ID seansu", example = "201")
        Long id,

        @Schema(description = "Informacje o filmie")
        MovieSimpleDto movie,

        @Schema(description = "Informacje o sali")
        RoomDto room,

        @Schema(description = "Czas rozpoczęcia", example = "2025-05-23T20:00:00")
        LocalDateTime startTime,

        @Schema(description = "Czas trwania")
        Duration duration,

        @Schema(description = "Lista dostępnych miejsc")
        List<SeatDto> seats

) {
}