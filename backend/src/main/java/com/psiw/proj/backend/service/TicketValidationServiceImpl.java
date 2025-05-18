package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.exeptions.custom.TicketNotFoundException;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.service.interfaces.TicketValidationService;
import com.psiw.proj.backend.utils.TicketStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
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

        if (!isValid(ticket)) {
            return ticket.getStatus();
        }

        if (isExpired(ticket)) {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);
            return TicketStatus.EXPIRED;
        }

        return TicketStatus.VALID;
    }

    @Override
    @Transactional
    public Ticket scanTicket(UUID ticketNumber) {
        TicketStatus status = checkTicket(ticketNumber);
        if (status != TicketStatus.VALID) {
            throw new IllegalStateException("Cannot scan ticket in status: " + status);
        }

        Ticket ticket = ticketRepository.getReferenceById(ticketNumber);
        ticket.setStatus(TicketStatus.USED);
        return ticketRepository.save(ticket);
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
