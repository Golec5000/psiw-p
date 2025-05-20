package com.psiw.proj.backend.utils.responseDto;

import com.psiw.proj.backend.utils.enums.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TicketResponse(
        List<Integer> seatNumbers,
        String movieTitle,
        LocalDateTime screeningStartTime,
        UUID ticketId,
        TicketStatus status,
        String email,
        String ticket_owner,
        BigDecimal price
) {
}