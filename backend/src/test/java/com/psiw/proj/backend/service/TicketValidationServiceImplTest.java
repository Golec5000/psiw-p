package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.*;
import com.psiw.proj.backend.exceptions.custom.TicketNotFoundException;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.service.implementation.TicketValidationServiceImpl;
import com.psiw.proj.backend.utils.enums.TicketStatus;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
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

    @Mock
    private Clock clock;

    @InjectMocks
    private TicketValidationServiceImpl service;

    private final UUID ticketId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.of(2025, 6, 5, 12, 0);

    @Test
    void shouldReturnValidWhenLessThan15MinutesBeforeStart() {
        // given
        prepareClock();
        LocalDateTime screeningStart = now.plusMinutes(10);
        Ticket ticket = prepareTicket(TicketStatus.WAITING_FOR_ACTIVATION, screeningStart);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // when
        TicketResponse response = service.checkTicket(ticketId);

        // then
        assertEquals(TicketStatus.VALID, response.status());
    }

    @Test
    void shouldKeepInactiveIfTooEarlyBeforeScreening() {
        // given
        prepareClock();
        LocalDateTime screeningStart = now.plusMinutes(30);
        Ticket ticket = prepareTicket(TicketStatus.WAITING_FOR_ACTIVATION, screeningStart);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // when
        TicketResponse response = service.checkTicket(ticketId);

        // then
        assertEquals(TicketStatus.WAITING_FOR_ACTIVATION, response.status());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldUpdateStatusToExpiredIfPastScreeningEnd() {
        // given
        prepareClock();
        LocalDateTime screeningStart = now.minusMinutes(100);
        Ticket ticket = prepareTicket(TicketStatus.VALID, screeningStart);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        TicketResponse response = service.checkTicket(ticketId);

        // then
        assertEquals(TicketStatus.EXPIRED, response.status());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldReturnTicketAsIsForUsedStatus() {
        // given
        prepareClock();
        LocalDateTime screeningStart = now.minusMinutes(20);
        Ticket ticket = prepareTicket(TicketStatus.USED, screeningStart);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // when
        TicketResponse response = service.checkTicket(ticketId);

        // then
        assertEquals(TicketStatus.USED, response.status());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowIfTicketNotFound() {
        // given
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(TicketNotFoundException.class, () -> service.checkTicket(ticketId));
    }

    @Test
    void shouldMarkTicketAsUsedIfValid() {
        // given
        prepareClock();
        LocalDateTime screeningStart = now.plusMinutes(5);
        Ticket ticket = prepareTicket(TicketStatus.VALID, screeningStart);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.getReferenceById(ticketId)).thenReturn(ticket);
        when(ticketRepository.save(any())).thenReturn(ticket);

        // when
        TicketResponse result = service.scanTicket(ticketId);

        // then
        assertEquals(TicketStatus.USED, result.status());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldThrowWhenScanningNonValidTicket() {
        // given
        prepareClock();
        LocalDateTime screeningStart = now.minusMinutes(10);
        Ticket ticket = prepareTicket(TicketStatus.EXPIRED, screeningStart);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // when & then
        assertThrows(IllegalStateException.class, () -> service.scanTicket(ticketId));
    }

    private void prepareClock() {
        Clock fixedClock = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    private Ticket prepareTicket(TicketStatus status, LocalDateTime screeningStart) {
        // Seat and ticketSeat
        Seat seat = new Seat();
        seat.setSeatNumber(1);
        TicketSeat ticketSeat = new TicketSeat();
        ticketSeat.setSeat(seat);

        // Movie
        Movie movie = new Movie();
        movie.setTitle("Example Movie");

        // Screening
        Screening screening = new Screening();
        screening.setStartTime(screeningStart);
        screening.setDuration(Duration.ofMinutes(90));
        screening.setMovie(movie);

        // Ticket
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
