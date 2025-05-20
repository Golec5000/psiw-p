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
import com.psiw.proj.backend.utils.requestDto.ReservationRequest;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.psiw.proj.backend.utils.DBInit.DEFAULT_SEAT_PRICE;

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
    public TicketResponse reserveSeats(ReservationRequest reservationRequest) {
        log.info("Reserving seats {} for screeningId {}", reservationRequest.seatIds(), reservationRequest.screeningId());

        Screening screening = screeningRepository.findById(reservationRequest.screeningId())
                .orElseThrow(() -> {
                    log.error("Screening not found: {}", reservationRequest.screeningId());
                    return new ScreeningNotFoundException("Screening not found: " + reservationRequest.screeningId());
                });

        Long roomNo = screening.getRoom().getRoomNumber();
        log.info("Screening {} is in room {}", reservationRequest.screeningId(), roomNo);

        long matching = seatRepository.countByIdInAndRoomRoomNumber(reservationRequest.seatIds(), roomNo);
        if (matching != reservationRequest.seatIds().size()) {
            log.warn("Seat validation failed for seats {} in room {}", reservationRequest.seatIds(), roomNo);
            throw new IllegalArgumentException("One or more seats not found or not in the same room");
        }

        Set<Long> taken = ticketRepository.findAllByScreeningId(reservationRequest.screeningId()).stream()
                .flatMap(ticket -> ticket.getTicketSeats().stream())
                .map(ts -> ts.getSeat().getId())
                .collect(Collectors.toSet());
        log.info("Currently taken seats for screening {}: {}", reservationRequest.screeningId(), taken);

        List<Long> conflict = reservationRequest.seatIds().stream()
                .filter(taken::contains)
                .toList();
        if (!conflict.isEmpty()) {
            log.warn("Attempt to reserve already taken seats: {}", conflict);
            throw new IllegalStateException("Seats already taken: " + conflict);
        }

        Ticket ticket = ticketRepository.save(
                Ticket.builder()
                        .screening(screening)
                        .ticketPrice(
                                DEFAULT_SEAT_PRICE
                                        .multiply(new BigDecimal(reservationRequest.seatIds().size()))
                                        .setScale(2, RoundingMode.HALF_UP)
                        )
                        .ownerName(reservationRequest.name())
                        .ownerSurname(reservationRequest.surname())
                        .ownerEmail(reservationRequest.email())
                        .status(TicketStatus.VALID)
                        .build()
        );
        log.info("Created ticket {} for screening {}", ticket.getTicketNumber(), reservationRequest.screeningId());

        List<Seat> seats = seatRepository.findAllById(reservationRequest.seatIds());

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
        log.info("Linked ticket {} with seats {}", ticket.getTicketNumber(), reservationRequest.seatIds());

        List<Integer> seatNumbers = seats.stream()
                .map(Seat::getSeatNumber)
                .toList();

        log.info("Reservation successful for ticket {}: seats {}", ticket.getTicketNumber(), seatNumbers);
        return new TicketResponse(
                seatNumbers,
                screening.getMovie().getTitle(),
                screening.getStartTime(),
                ticket.getTicketNumber(),
                ticket.getStatus(),
                ticket.getOwnerEmail(),
                ticket.getOwnerName() + " " + ticket.getOwnerSurname(),
                ticket.getTicketPrice()
        );
    }
}
