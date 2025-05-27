package com.psiw.proj.backend.service.implementation;

import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.exceptions.custom.TicketNotFoundException;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.service.interfaces.TicketValidationService;
import com.psiw.proj.backend.utils.enums.TicketStatus;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketValidationServiceImpl implements TicketValidationService {

    private final TicketRepository ticketRepository;
    private final Clock clock;

    @Override
    @Transactional
    public TicketStatus checkTicket(UUID ticketNumber) {
        Ticket ticket = ticketRepository.findById(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketNumber));

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = ticket.getScreening().getStartTime();
        LocalDateTime end = start.plus(ticket.getScreening().getDuration());

        // 1) TO_BE_CALCULATED → VALID, jeżeli jesteśmy w oknie 15 min przed seansem
        if (ticket.getStatus() == TicketStatus.TO_BE_CALCULATED) {
            if (!now.isBefore(start.minusMinutes(15)) && now.isBefore(start)) {
                ticket.setStatus(TicketStatus.VALID);
                ticketRepository.save(ticket);
                return TicketStatus.VALID;
            }
            return TicketStatus.TO_BE_CALCULATED;
        }

        // 2) VALID → EXPIRED, jeżeli seans się skończył
        if (ticket.getStatus() == TicketStatus.VALID) {
            if (now.isAfter(end) || now.isEqual(end)) {
                ticket.setStatus(TicketStatus.EXPIRED);
                ticketRepository.save(ticket);
                return TicketStatus.EXPIRED;
            }
            return TicketStatus.VALID;
        }

        // 3) pozostałe statusy (USED, EXPIRED itd.) zwracamy bez zmian
        return ticket.getStatus();
    }

    @Override
    @Transactional
    public TicketResponse scanTicket(UUID ticketNumber) {
        // najpierw uaktualniamy status
        TicketStatus current = checkTicket(ticketNumber);

        if (current != TicketStatus.VALID) {
            throw new IllegalStateException("Cannot scan ticket in status: " + current);
        }

        // oznaczamy jako USED
        Ticket ticket = ticketRepository.getReferenceById(ticketNumber);
        ticket.setStatus(TicketStatus.USED);
        Ticket updated = ticketRepository.save(ticket);

        return mapToTicketResponse(updated);
    }

    private TicketResponse mapToTicketResponse(Ticket ticket) {
        List<Integer> seats = ticket.getTicketSeats().stream()
                .map(ts -> ts.getSeat().getSeatNumber())
                .toList();

        return TicketResponse.builder()
                .ticketId(ticket.getTicketNumber())
                .seatNumbers(seats)
                .movieTitle(ticket.getScreening().getMovie().getTitle())
                .screeningStartTime(ticket.getScreening().getStartTime())
                .status(ticket.getStatus())
                .email(ticket.getOwnerEmail())
                .ticketOwner(ticket.getOwnerName() + " " + ticket.getOwnerSurname())
                .price(ticket.getTicketPrice())
                .build();
    }
}

