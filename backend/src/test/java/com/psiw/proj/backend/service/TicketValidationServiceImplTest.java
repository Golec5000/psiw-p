package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.*;
import com.psiw.proj.backend.exceptions.custom.TicketNotFoundException;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.service.implementation.TicketValidationServiceImpl;
import com.psiw.proj.backend.utils.enums.TicketStatus;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketValidationServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    // REMOVE the mocked Clock
    // @Mock private Clock clock;

    // Do NOT use @InjectMocks since we'll construct the service manually
    private TicketValidationServiceImpl service;

    private final UUID ticketId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.of(2025, 6, 5, 12, 0);

    @BeforeEach
    void setUp() {
        // Match the zone used by the service
        ZoneId zone = ZoneId.of("UTC+2");
        Clock fixedClock = Clock.fixed(now.atZone(zone).toInstant(), zone);
        service = new TicketValidationServiceImpl(ticketRepository, fixedClock);
    }

    @Test
    void shouldReturnValidWhenLessThan15MinutesBeforeStart() {
        LocalDateTime screeningStart = now.plusMinutes(10);
        Ticket ticket = prepareTicket(TicketStatus.WAITING_FOR_ACTIVATION, screeningStart);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse response = service.checkTicket(ticketId);

        assertEquals(TicketStatus.VALID, response.status());
    }

    @Test
    void shouldKeepInactiveIfTooEarlyBeforeScreening() {
        LocalDateTime screeningStart = now.plusMinutes(30);
        Ticket ticket = prepareTicket(TicketStatus.WAITING_FOR_ACTIVATION, screeningStart);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        TicketResponse response = service.checkTicket(ticketId);

        assertEquals(TicketStatus.WAITING_FOR_ACTIVATION, response.status());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldUpdateStatusToExpiredIfPastScreeningEnd() {
        LocalDateTime screeningStart = now.minusMinutes(100); // end was 10 min ago (90 min duration)
        Ticket ticket = prepareTicket(TicketStatus.VALID, screeningStart);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse response = service.checkTicket(ticketId);

        assertEquals(TicketStatus.EXPIRED, response.status());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldReturnTicketAsIsForUsedStatus() {
        LocalDateTime screeningStart = now.minusMinutes(20);
        Ticket ticket = prepareTicket(TicketStatus.USED, screeningStart);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        TicketResponse response = service.checkTicket(ticketId);

        assertEquals(TicketStatus.USED, response.status());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowIfTicketNotFound() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());
        assertThrows(TicketNotFoundException.class, () -> service.checkTicket(ticketId));
    }

    @Test
    void shouldMarkTicketAsUsedIfValid() {
        LocalDateTime screeningStart = now.plusMinutes(5);
        Ticket ticket = prepareTicket(TicketStatus.VALID, screeningStart);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.getReferenceById(ticketId)).thenReturn(ticket);
        when(ticketRepository.save(any())).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            // ensure persisted status is USED
            return t;
        });

        TicketResponse result = service.scanTicket(ticketId);

        assertEquals(TicketStatus.USED, result.status());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldThrowWhenScanningNonValidTicket() {
        LocalDateTime screeningStart = now.minusMinutes(10);
        Ticket ticket = prepareTicket(TicketStatus.EXPIRED, screeningStart);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(IllegalStateException.class, () -> service.scanTicket(ticketId));
    }

    private Ticket prepareTicket(TicketStatus status, LocalDateTime screeningStart) {
        Seat seat = new Seat();
        seat.setSeatNumber(1);
        TicketSeat ticketSeat = new TicketSeat();
        ticketSeat.setSeat(seat);

        Movie movie = new Movie();
        movie.setTitle("Example Movie");

        Screening screening = new Screening();
        screening.setStartTime(screeningStart);
        screening.setDuration(Duration.ofMinutes(90));
        screening.setMovie(movie);

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(ticketId);
        ticket.setStatus(status);
        ticket.setTicketPrice(BigDecimal.valueOf(30.0));
        ticket.setOwnerEmail("test@example.com");
        ticket.setOwnerName("John");
        ticket.setOwnerSurname("Doe");
        ticket.setScreening(screening);
        ticket.setTicketSeats(List.of(ticketSeat));
        return ticket;
    }
}
