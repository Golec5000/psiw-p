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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final TicketSeatRepository ticketSeatRepository;

    @Override
    @Transactional
    public Ticket reserveSeats(Long screeningId, List<Long> seatIds) {
        // 1. Pobierz seans
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ScreeningNotFoundException("Screening not found: " + screeningId));

        Long roomNo = screening.getRoom().getRoomNumber();

        // 2. Sprawdź w jednym zapytaniu: czy wszystkie miejsca istnieją i są w tej samej sali?
        long matching = seatRepository.countByIdInAndRoomRoomNumber(seatIds, roomNo);
        if (matching != seatIds.size()) {
            throw new IllegalArgumentException("One or more seats not found or not in the same room");
        }

        // 3. Zbierz już zajęte miejsca (EntityGraph ładuje od razu ticketSeats + seat)
        Set<Long> taken = ticketRepository.findAllByScreeningId(screeningId).stream()
                .flatMap(ticket -> ticket.getTicketSeats().stream())
                .map(ts -> ts.getSeat().getId())
                .collect(Collectors.toSet());

        List<Long> conflict = seatIds.stream()
                .filter(taken::contains)
                .toList();
        if (!conflict.isEmpty()) {
            throw new IllegalStateException("Seats already taken: " + conflict);
        }

        // 4. Stwórz sam Ticket
        Ticket ticket = ticketRepository.save(
                Ticket.builder()
                        .screening(screening)
                        .status(TicketStatus.VALID)
                        .build()
        );

        // 5. Wczytaj encje Seat (tu już tylko po id, bez ładowania room ponownie)
        List<Seat> seats = seatRepository.findAllById(seatIds);

        // 6. Stwórz i zapisz powiązania ticket_seat
        List<TicketSeat> links = seats.stream()
                .map(seat -> TicketSeat.builder()
                        .ticket(ticket)
                        .seat(seat)
                        .screening(screening)
                        .build()
                )
                .toList();
        ticketSeatRepository.saveAll(links);

        // 7. Podłącz zapisane linki do Ticket i zwróć
        ticket.setTicketSeats(links);
        return ticket;
    }
}
