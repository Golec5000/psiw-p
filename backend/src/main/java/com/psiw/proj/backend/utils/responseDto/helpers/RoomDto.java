package com.psiw.proj.backend.utils.responseDto.helpers;

import lombok.Builder;

@Builder
public record RoomDto(
        Long roomNumber,
        int rowCount,
        int columnCount
) {}
