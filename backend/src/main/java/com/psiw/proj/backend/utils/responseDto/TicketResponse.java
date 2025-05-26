package com.psiw.proj.backend.utils.responseDto;

import com.psiw.proj.backend.utils.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Odpowiedź po potwierdzeniu rezerwacji zawierająca dane biletu")
@Builder
public record TicketResponse(

        @Schema(description = "Numery miejsc", example = "[12, 13]")
        List<Integer> seatNumbers,

        @Schema(description = "Tytuł filmu", example = "Interstellar")
        String movieTitle,

        @Schema(description = "Data i godzina rozpoczęcia seansu", example = "2025-05-24T18:30:00")
        LocalDateTime screeningStartTime,

        @Schema(description = "Unikalny identyfikator biletu", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID ticketId,

        @Schema(description = "Status biletu", example = "CONFIRMED")
        TicketStatus status,

        @Schema(description = "Email osoby rezerwującej", example = "john.doe@example.com")
        String email,

        @Schema(description = "Imię i nazwisko właściciela biletu", example = "John Doe")
        String ticketOwner,

        @Schema(description = "Łączna cena biletu", example = "59.99")
        BigDecimal price

) {}