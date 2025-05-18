package com.psiw.proj.backend.utils.responseDto;

import java.time.LocalDateTime;
import java.util.List;

public record TicketResponse(
        List<String> seatNumbers,
        String movieTitle,
        LocalDateTime screeningStartTime
) {
}