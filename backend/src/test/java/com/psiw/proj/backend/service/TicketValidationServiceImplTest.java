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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketValidationServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    @Spy
    private TicketValidationServiceImpl service;

    // "stały" zegar dla wszystkich testów
    private static final LocalDateTime fixedNow = LocalDateTime.of(2025, 5, 25, 12, 0);
    private static final Clock fixedClock = Clock.fixed(
            fixedNow.atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );

    private static final UUID ticketId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @BeforeEach
    void initClock() {
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    private Ticket buildTicket(TicketStatus status, LocalDateTime start, Duration duration) {
        Screening screening = Screening.builder()
                .startTime(start)
                .duration(duration)
                .build();
        return Ticket.builder()
                .ticketNumber(ticketId)
                .status(status)
                .screening(screening)
                .ownerEmail("test@example.com")
                .ownerName("John")
                .ownerSurname("Doe")
                .ticketPrice(BigDecimal.TEN)
                .ticketSeats(List.of())
                .build();
    }

    @Test
    void shouldThrowIfTicketNotFound() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.checkTicket(ticketId))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining("Ticket not found");
    }

    @Test
    void shouldNotChangeStatusIfToBeCalculatedOutsideWindow() {
        LocalDateTime now = LocalDateTime.of(2025, 5, 25, 12, 0);
        Clock fixed = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixed.instant());
        when(clock.getZone()).thenReturn(fixed.getZone());

        // start 20 minutes ahead → outside 15' window
        Ticket ticket = buildTicket(TicketStatus.TO_BE_CALCULATED, now.plusMinutes(20), Duration.ofMinutes(60));
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        TicketStatus status = service.checkTicket(ticketId);

        assertThat(status).isEqualTo(TicketStatus.TO_BE_CALCULATED);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldValidateIfToBeCalculatedWithinWindow() {
        LocalDateTime now = LocalDateTime.of(2025, 5, 25, 12, 0);
        Clock fixed = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixed.instant());
        when(clock.getZone()).thenReturn(fixed.getZone());

        // start in 10 minutes → inside 15' window
        Ticket ticket = buildTicket(TicketStatus.TO_BE_CALCULATED, now.plusMinutes(10), Duration.ofMinutes(60));
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketStatus status = service.checkTicket(ticketId);

        assertThat(status).isEqualTo(TicketStatus.VALID);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.VALID);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldReturnValidForValidTicketBeforeEnd() {
        LocalDateTime now = LocalDateTime.of(2025, 5, 25, 11, 0);
        Clock fixed = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixed.instant());
        when(clock.getZone()).thenReturn(fixed.getZone());

        // start at 10:00, duration 120' → ends at 12:00
        Ticket ticket = buildTicket(TicketStatus.VALID, now.minusHours(1), Duration.ofMinutes(120));
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        TicketStatus status = service.checkTicket(ticketId);

        assertThat(status).isEqualTo(TicketStatus.VALID);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldExpireValidTicketAfterEnd() {
        LocalDateTime now = LocalDateTime.of(2025, 5, 25, 13, 0);
        Clock fixed = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixed.instant());
        when(clock.getZone()).thenReturn(fixed.getZone());

        // start at 10:00, duration 120' → ends at 12:00, now=13:00 → expired
        Ticket ticket = buildTicket(TicketStatus.VALID, now.minusHours(3), Duration.ofMinutes(120));
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketStatus status = service.checkTicket(ticketId);

        assertThat(status).isEqualTo(TicketStatus.EXPIRED);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.EXPIRED);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldReturnExistingNonValidStatusWithoutSave() {
        Ticket ticket = buildTicket(TicketStatus.USED, LocalDateTime.now(), Duration.ZERO);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        TicketStatus status = service.checkTicket(ticketId);

        assertThat(status).isEqualTo(TicketStatus.USED);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenScanningNonValid() {
        Ticket ticket = buildTicket(TicketStatus.EXPIRED, LocalDateTime.now(), Duration.ZERO);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.scanTicket(ticketId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot scan ticket in status: EXPIRED");
    }

    @Test
    void shouldScanValidTicketAndReturnProperResponse() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 5, 25, 11, 0);
        Clock fixed = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixed.instant());
        when(clock.getZone()).thenReturn(fixed.getZone());

        // przygotuj powiązane encje
        Screening screening = Screening.builder()
                .startTime(now.minusMinutes(30))
                .duration(Duration.ofMinutes(120))
                .movie(Movie.builder()
                        .title("Some Movie")
                        .build())
                .build();

        Seat seat = Seat.builder()
                .seatNumber(7)
                .build();

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketId)
                .status(TicketStatus.VALID)
                .screening(screening)
                .ownerEmail("user@example.com")
                .ownerName("Jane")
                .ownerSurname("Smith")
                .ticketPrice(BigDecimal.valueOf(30))
                .build();
        // ustaw listę miejsc
        TicketSeat ts = TicketSeat.builder()
                .seat(seat)
                .screening(screening)
                .ticket(ticket)
                .build();
        ticket.setTicketSeats(List.of(ts));

        // stuby repo
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.getReferenceById(ticketId)).thenReturn(ticket);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        TicketResponse response = service.scanTicket(ticketId);

        // then
        assertThat(response.ticketId()).isEqualTo(ticketId);
        assertThat(response.status()).isEqualTo(TicketStatus.USED);
        assertThat(response.movieTitle()).isEqualTo("Some Movie");
        assertThat(response.screeningStartTime()).isEqualTo(screening.getStartTime());
        assertThat(response.seatNumbers()).containsExactly(7);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.ticketOwner()).isEqualTo("Jane Smith");
        assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(30));

        // i status encji też się zmienił
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.USED);
        verify(ticketRepository).save(ticket);
    }
}
