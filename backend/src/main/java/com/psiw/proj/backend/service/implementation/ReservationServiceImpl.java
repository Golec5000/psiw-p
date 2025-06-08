package com.psiw.proj.backend.service.implementation;

import com.psiw.proj.backend.entity.Screening;
import com.psiw.proj.backend.entity.Seat;
import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.entity.TicketSeat;
import com.psiw.proj.backend.exceptions.custom.ScreeningNotFoundException;
import com.psiw.proj.backend.repository.ScreeningRepository;
import com.psiw.proj.backend.repository.SeatRepository;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.repository.TicketSeatRepository;
import com.psiw.proj.backend.service.interfaces.ReservationService;
import com.psiw.proj.backend.utils.enums.TicketStatus;
import com.psiw.proj.backend.utils.requestDto.ReservationRequest;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.psiw.proj.backend.utils.DBInit.DEFAULT_SEAT_PRICE;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final TicketSeatRepository ticketSeatRepository;

    @Override
    @Transactional
    public TicketResponse reserveSeats(ReservationRequest reservationRequest) {
        Screening screening = screeningRepository.findById(reservationRequest.screeningId())
                .orElseThrow(() -> new ScreeningNotFoundException("Screening not found: " + reservationRequest.screeningId()));

        String roomNo = screening.getRoom().getRoomNumber();
        long matching = seatRepository.countByIdInAndRoomRoomNumber(reservationRequest.seatIds(), roomNo);
        if (matching != reservationRequest.seatIds().size())
            throw new IllegalArgumentException("One or more seats not found or not in the same room");

        Set<Long> taken = extractTakenSeats(reservationRequest);
        List<Long> conflict = reservationRequest.seatIds().stream()
                .filter(taken::contains)
                .toList();

        if (!conflict.isEmpty()) throw new IllegalStateException("Seats already taken: " + conflict);

        Ticket ticket = ticketRepository.save(createTicket(reservationRequest, screening));
        List<Seat> seats = seatRepository.findAllById(reservationRequest.seatIds());
        List<TicketSeat> links = getTicketSeats(seats, ticket, screening);

        ticketSeatRepository.saveAll(links);
        ticket.setTicketSeats(links);

        return createTicketResponse(seats, screening, ticket);
    }

    private TicketResponse createTicketResponse(List<Seat> seats, Screening screening, Ticket ticket) {
        return TicketResponse.builder()
                .seatNumbers(getSeatNumbers(seats))
                .movieTitle(screening.getMovie().getTitle())
                .screeningStartTime(screening.getStartTime())
                .ticketId(ticket.getTicketNumber())
                .status(ticket.getStatus())
                .email(ticket.getOwnerEmail())
                .ticketOwner(ticket.getOwnerName() + " " + ticket.getOwnerSurname())
                .price(ticket.getTicketPrice())
                .build();
    }

    private Set<Long> extractTakenSeats(ReservationRequest reservationRequest) {
        return ticketRepository.findAllByScreeningId(reservationRequest.screeningId()).stream()
                .flatMap(ticket -> ticket.getTicketSeats().stream())
                .map(ts -> ts.getSeat().getId())
                .collect(Collectors.toSet());
    }

    private List<Integer> getSeatNumbers(List<Seat> seats) {
        return seats.stream()
                .map(Seat::getSeatNumber)
                .toList();
    }

    private List<TicketSeat> getTicketSeats(List<Seat> seats, Ticket ticket, Screening screening) {
        return seats.stream()
                .map(seat -> TicketSeat.builder()
                        .ticket(ticket)
                        .seat(seat)
                        .screening(screening)
                        .build()
                )
                .toList();
    }

    private Ticket createTicket(ReservationRequest reservationRequest, Screening screening) {
        return Ticket.builder()
                .screening(screening)
                .ticketPrice(
                        DEFAULT_SEAT_PRICE
                                .multiply(new BigDecimal(reservationRequest.seatIds().size()))
                                .setScale(2, RoundingMode.HALF_UP)
                )
                .ownerName(reservationRequest.name())
                .ownerSurname(reservationRequest.surname())
                .ownerEmail(reservationRequest.email())
                .status(TicketStatus.WAITING_FOR_ACTIVATION)
                .build();
    }
}
