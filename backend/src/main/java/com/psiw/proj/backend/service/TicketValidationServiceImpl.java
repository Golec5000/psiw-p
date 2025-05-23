package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.exeptions.custom.TicketNotFoundException;
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
        Ticket ticket = findExistingTicket(ticketNumber);

        if (!isValid(ticket)) return ticket.getStatus();

        if (isExpired(ticket)) {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);
            return TicketStatus.EXPIRED;
        }

        return TicketStatus.VALID;
    }

    @Override
    @Transactional
    public TicketResponse scanTicket(UUID ticketNumber) {
        TicketStatus status = checkTicket(ticketNumber);

        if (status != TicketStatus.VALID)
            throw new IllegalStateException("Cannot scan ticket in status: " + status);

        Ticket ticket = ticketRepository.getReferenceById(ticketNumber);
        ticket.setStatus(TicketStatus.USED);
        Ticket updatedTicket = ticketRepository.save(ticket);

        return mapToTicketResponse(updatedTicket);
    }

    TicketResponse mapToTicketResponse(Ticket ticket) {
        List<Integer> seatNumbers = ticket.getTicketSeats().stream()
                .map(screening -> screening.getSeat().getSeatNumber())
                .toList();
        String movieTitle = ticket.getScreening().getMovie().getTitle();
        LocalDateTime screeningStartTime = ticket.getScreening().getStartTime();
        UUID ticketId = ticket.getTicketNumber();

        return TicketResponse.builder()
                .seatNumbers(seatNumbers)
                .movieTitle(movieTitle)
                .screeningStartTime(screeningStartTime)
                .ticketId(ticketId)
                .status(ticket.getStatus())
                .email(ticket.getOwnerEmail())
                .ticket_owner(ticket.getOwnerName() + " " + ticket.getOwnerSurname())
                .price(ticket.getTicketPrice())
                .build();
    }

    private Ticket findExistingTicket(UUID ticketNumber) {
        return ticketRepository.findById(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketNumber));
    }

    private boolean isValid(Ticket ticket) {
        return ticket.getStatus() == TicketStatus.VALID;
    }

    private boolean isExpired(Ticket ticket) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = ticket.getScreening().getStartTime();
        LocalDateTime end = start.plus(ticket.getScreening().getDuration());
        return now.isAfter(end);
    }
}
