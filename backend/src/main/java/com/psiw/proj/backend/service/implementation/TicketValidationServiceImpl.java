package com.psiw.proj.backend.service.implementation;

import com.psiw.proj.backend.entity.Screening;
import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.exceptions.custom.TicketNotFoundException;
import com.psiw.proj.backend.repository.ScreeningRepository;
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
    private final ScreeningRepository screeningRepository;
    private final Clock clock;

    @Override
    @Transactional
    public TicketStatus checkTicket(UUID ticketNumber) {
        return findExistingTicket(ticketNumber).getStatus();
    }

    @Override
    @Transactional
    public TicketResponse scanTicket(UUID ticketNumber) {
        Ticket ticket = findExistingTicket(ticketNumber);

        TicketStatus status = evaluateStatus(ticket);
        if (status != TicketStatus.VALID) {
            throw new IllegalStateException("Cannot scan ticket in status: " + status);
        }

        ticket.setStatus(TicketStatus.USED);
        Ticket updated = ticketRepository.save(ticket);

        return mapToTicketResponse(updated);
    }

    @Override
    @Transactional
    public int updateTicketStatus() {
        LocalDateTime now = LocalDateTime.now(clock);

        // 1) z TO_BE_CALCULATED → VALID
        int toValid = ticketRepository.updateStatusToValid(
                TicketStatus.VALID,
                TicketStatus.TO_BE_CALCULATED,
                now,
                now.plusMinutes(15)
        );

        // 2) Znajdź seanse, które już się zaczęły
        List<Screening> started = screeningRepository.findStartedScreenings(now);

        // 3) Odfiltruj seanse, które już się skończyły
        List<Long> expiredIds = started.stream()
                .filter(s -> s.getStartTime().plus(s.getDuration()).isBefore(now)
                        || s.getStartTime().plus(s.getDuration()).isEqual(now))
                .map(Screening::getId)
                .toList();

        return toValid + ticketRepository.expirePastTicketsByScreening(expiredIds);
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

    private TicketStatus evaluateStatus(Ticket ticket) {
        if (ticket.getStatus() != TicketStatus.VALID) {
            return ticket.getStatus();
        }
        return isExpired(ticket) ? TicketStatus.EXPIRED : TicketStatus.VALID;
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
                .ticketOwner(ticket.getOwnerName() + " " + ticket.getOwnerSurname())
                .price(ticket.getTicketPrice())
                .build();
    }
}
