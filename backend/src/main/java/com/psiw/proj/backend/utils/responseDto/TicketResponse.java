package com.psiw.proj.backend.utils.responseDto;

import com.psiw.proj.backend.utils.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TicketResponse(
        List<String> seatNumbers,
        String movieTitle,
        LocalDateTime screeningStartTime,
        UUID ticketId,
        TicketStatus status
) {
}