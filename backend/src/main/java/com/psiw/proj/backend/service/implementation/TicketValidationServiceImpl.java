package com.psiw.proj.backend.service.implementation;

import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.exceptions.custom.TicketNotFoundException;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.service.interfaces.TicketValidationService;
import com.psiw.proj.backend.utils.enums.TicketStatus;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
import jakarta.annotation.Nullable;
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
    public TicketResponse checkTicket(UUID ticketNumber) {
        Ticket ticket = ticketRepository.findById(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketNumber));

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = ticket.getScreening().getStartTime();
        LocalDateTime end = start.plus(ticket.getScreening().getDuration());

        // WAITING_FOR_ACTIVATION
        if (ticket.getStatus() == TicketStatus.WAITING_FOR_ACTIVATION) {
            // ticket becomes active 15 minutes before the film starts
            if (!now.isBefore(start.minusMinutes(15)) && now.isBefore(end)) {
                ticket.setStatus(TicketStatus.VALID);
                ticketRepository.save(ticket);
                return mapToTicketResponse(ticket, null);
            }
            return mapToTicketResponse(ticket, null);
        }

        // VALID
        if (ticket.getStatus() == TicketStatus.VALID) {
            if (now.isAfter(end) || now.isEqual(end)) {
                ticket.setStatus(TicketStatus.EXPIRED);
                ticketRepository.save(ticket);
                return mapToTicketResponse(ticket, null);
            }
            return mapToTicketResponse(ticket, null);
        }

        // 3) pozostałe statusy (USED, EXPIRED itd.) zwracamy bez zmian
        return mapToTicketResponse(ticket, null);
    }

    @Override
    @Transactional
    public TicketResponse scanTicket(UUID ticketNumber) {
        // najpierw uaktualniamy status
        TicketStatus current = checkTicket(ticketNumber).status();

        if (current != TicketStatus.VALID) {
            throw new IllegalStateException("Cannot scan ticket in status: " + current);
        }

        // oznaczamy jako USED
        Ticket ticket = ticketRepository.getReferenceById(ticketNumber);
        ticket.setStatus(TicketStatus.USED);
        Ticket updated = ticketRepository.save(ticket);

        return mapToTicketResponse(updated, null);
    }

    private TicketResponse mapToTicketResponse(Ticket ticket, @Nullable TicketStatus status) {
        List<Integer> seats = ticket.getTicketSeats().stream()
                .map(ts -> ts.getSeat().getSeatNumber())
                .toList();

        return TicketResponse.builder()
                .ticketId(ticket.getTicketNumber())
                .seatNumbers(seats)
                .movieTitle(ticket.getScreening().getMovie().getTitle())
                .screeningStartTime(ticket.getScreening().getStartTime())
                .status(status == null ? ticket.getStatus() : status)
                .email(ticket.getOwnerEmail())
                .ticketOwner(ticket.getOwnerName() + " " + ticket.getOwnerSurname())
                .price(ticket.getTicketPrice())
                .build();
    }
}

