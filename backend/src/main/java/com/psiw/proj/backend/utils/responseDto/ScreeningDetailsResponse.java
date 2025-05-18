package com.psiw.proj.backend.utils.responseDto;

import com.psiw.proj.backend.utils.responseDto.helpers.MovieSimpleDto;
import com.psiw.proj.backend.utils.responseDto.helpers.RoomDto;
import com.psiw.proj.backend.utils.responseDto.helpers.SeatDto;
import lombok.Builder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ScreeningDetailsResponse(
        Long id,
        MovieSimpleDto movie,
        RoomDto room,
        LocalDateTime startTime,
        Duration duration,
        List<SeatDto> seats      // cała pula miejsc wraz z flagą dostępności
) {}