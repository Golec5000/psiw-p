package com.psiw.proj.backend.utils.responseDto.helpers;

import lombok.Builder;

@Builder
public record RoomDto(
        String roomNumber,
        int rowCount,
        int columnCount
) {}
