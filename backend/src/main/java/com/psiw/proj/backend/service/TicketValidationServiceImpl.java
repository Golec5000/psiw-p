package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.exeptions.custom.TicketNotFoundException;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.service.interfaces.TicketValidationService;
import com.psiw.proj.backend.utils.TicketStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketValidationServiceImpl implements TicketValidationService {

    private final TicketRepository ticketRepository;
    private final Clock clock;

    @Override
    @Transactional
    public TicketStatus checkTicket(UUID ticketNumber) {
        log.info("Checking ticket with number: {}", ticketNumber);
        Ticket ticket = findExistingTicket(ticketNumber);

        if (!isValid(ticket)) {
            log.warn("Ticket {} is not valid. Current status: {}", ticketNumber, ticket.getStatus());
            return ticket.getStatus();
        }

        if (isExpired(ticket)) {
            log.info("Ticket {} has expired. Marking as EXPIRED.", ticketNumber);
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);
            return TicketStatus.EXPIRED;
        }

        log.info("Ticket {} is valid.", ticketNumber);
        return TicketStatus.VALID;
    }

    @Override
    @Transactional
    public Ticket scanTicket(UUID ticketNumber) {
        log.info("Scanning ticket with number: {}", ticketNumber);
        TicketStatus status = checkTicket(ticketNumber);
        if (status != TicketStatus.VALID) {
            log.error("Cannot scan ticket {}. Status: {}", ticketNumber, status);
            throw new IllegalStateException("Cannot scan ticket in status: " + status);
        }

        Ticket ticket = ticketRepository.getReferenceById(ticketNumber);
        ticket.setStatus(TicketStatus.USED);
        Ticket updatedTicket = ticketRepository.save(ticket);
        log.info("Ticket {} scanned successfully. Status set to USED.", ticketNumber);
        return updatedTicket;
    }

    private Ticket findExistingTicket(UUID ticketNumber) {
        log.debug("Finding ticket with number: {}", ticketNumber);
        return ticketRepository.findById(ticketNumber)
                .orElseThrow(() -> {
                    log.error("Ticket not found: {}", ticketNumber);
                    return new TicketNotFoundException("Ticket not found: " + ticketNumber);
                });
    }

    private boolean isValid(Ticket ticket) {
        boolean valid = ticket.getStatus() == TicketStatus.VALID;
        log.debug("Ticket {} valid status: {}", ticket.getTicketNumber(), valid);
        return valid;
    }

    private boolean isExpired(Ticket ticket) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = ticket.getScreening().getStartTime();
        LocalDateTime end = start.plus(ticket.getScreening().getDuration());
        boolean expired = now.isAfter(end);
        log.debug("Ticket {} expired check: now={}, end={}, expired={}", ticket.getTicketNumber(), now, end, expired);
        return expired;
    }
}
