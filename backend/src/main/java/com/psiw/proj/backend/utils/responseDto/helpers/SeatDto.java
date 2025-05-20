package com.psiw.proj.backend.utils.responseDto.helpers;

import lombok.Builder;

@Builder
public record SeatDto(
        Long id,
        int rowNumber,
        int columnNumber,
        int seatNumber,
        boolean available
) {}
