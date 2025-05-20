package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.Screening;
import com.psiw.proj.backend.entity.Seat;
import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.entity.TicketSeat;
import com.psiw.proj.backend.exeptions.custom.ScreeningNotFoundException;
import com.psiw.proj.backend.repository.ScreeningRepository;
import com.psiw.proj.backend.repository.SeatRepository;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.repository.TicketSeatRepository;
import com.psiw.proj.backend.service.interfaces.ReservationService;
import com.psiw.proj.backend.utils.TicketStatus;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final TicketSeatRepository ticketSeatRepository;

    @Override
    @Transactional
    public TicketResponse reserveSeats(Long screeningId, List<Long> seatIds) {
        log.info("Reserving seats {} for screeningId {}", seatIds, screeningId);

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> {
                    log.error("Screening not found: {}", screeningId);
                    return new ScreeningNotFoundException("Screening not found: " + screeningId);
                });

        Long roomNo = screening.getRoom().getRoomNumber();
        log.info("Screening {} is in room {}", screeningId, roomNo);

        long matching = seatRepository.countByIdInAndRoomRoomNumber(seatIds, roomNo);
        if (matching != seatIds.size()) {
            log.warn("Seat validation failed for seats {} in room {}", seatIds, roomNo);
            throw new IllegalArgumentException("One or more seats not found or not in the same room");
        }

        Set<Long> taken = ticketRepository.findAllByScreeningId(screeningId).stream()
                .flatMap(ticket -> ticket.getTicketSeats().stream())
                .map(ts -> ts.getSeat().getId())
                .collect(Collectors.toSet());
        log.info("Currently taken seats for screening {}: {}", screeningId, taken);

        List<Long> conflict = seatIds.stream()
                .filter(taken::contains)
                .toList();
        if (!conflict.isEmpty()) {
            log.warn("Attempt to reserve already taken seats: {}", conflict);
            throw new IllegalStateException("Seats already taken: " + conflict);
        }

        Ticket ticket = ticketRepository.save(
                Ticket.builder()
                        .screening(screening)
                        .status(TicketStatus.VALID)
                        .build()
        );
        log.info("Created ticket {} for screening {}", ticket.getTicketNumber(), screeningId);

        List<Seat> seats = seatRepository.findAllById(seatIds);

        List<TicketSeat> links = seats.stream()
                .map(seat -> TicketSeat.builder()
                        .ticket(ticket)
                        .seat(seat)
                        .screening(screening)
                        .build()
                )
                .toList();
        ticketSeatRepository.saveAll(links);
        ticket.setTicketSeats(links);
        log.info("Linked ticket {} with seats {}", ticket.getTicketNumber(), seatIds);

        List<String> seatNumbers = seats.stream()
                .map(seat -> String.format("R%dC%d", seat.getRowNumber(), seat.getColumnNumber()))
                .toList();

        log.info("Reservation successful for ticket {}: seats {}", ticket.getTicketNumber(), seatNumbers);
        return new TicketResponse(
                seatNumbers,
                screening.getMovie().getTitle(),
                screening.getStartTime(),
                ticket.getTicketNumber(),
                ticket.getStatus()
        );
    }
}
