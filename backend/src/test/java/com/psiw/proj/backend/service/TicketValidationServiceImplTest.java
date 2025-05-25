package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.Movie;
import com.psiw.proj.backend.entity.Screening;
import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.exceptions.custom.TicketNotFoundException;
import com.psiw.proj.backend.repository.ScreeningRepository;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.utils.enums.TicketStatus;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.psiw.proj.backend.utils.DBInit.DEFAULT_SEAT_PRICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketValidationServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    @Spy
    private TicketValidationServiceImpl ticketValidationService;

    @Test
    void shouldReturnValidStatusForNonExpiredTicket() {
        // given
        UUID ticketId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2025, 5, 20, 10, 0);
        Clock fixedClock = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        Screening screening = Screening.builder()
                .startTime(now.minusMinutes(10))
                .duration(Duration.ofMinutes(30))
                .build();

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketId)
                .status(TicketStatus.VALID)
                .screening(screening)
                .build();

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // when
        TicketStatus result = ticketValidationService.checkTicket(ticketId);

        // then
        assertThat(result).isEqualTo(TicketStatus.VALID);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldReturnExpiredStatusForExpiredTicket() {
        // given
        UUID ticketId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2025, 5, 20, 12, 0);
        Clock fixedClock = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        Screening screening = Screening.builder()
                .startTime(now.minusHours(2))
                .duration(Duration.ofMinutes(60))
                .build();

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketId)
                .status(TicketStatus.VALID)
                .screening(screening)
                .build();

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenReturn(ticket);

        // when
        TicketStatus result = ticketValidationService.checkTicket(ticketId);

        // then
        assertThat(result).isEqualTo(TicketStatus.EXPIRED);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.EXPIRED);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void shouldReturnStatusIfTicketNotValid() {
        // given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketId)
                .status(TicketStatus.USED) // already used
                .screening(mock(Screening.class))
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // when
        TicketStatus result = ticketValidationService.checkTicket(ticketId);

        // then
        assertThat(result).isEqualTo(TicketStatus.USED);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowIfTicketNotFound() {
        // given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> ticketValidationService.checkTicket(ticketId))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining("Ticket not found");
    }

    @Test
    void shouldScanValidTicket() {
        // given
        UUID ticketId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2025, 5, 20, 9, 0);
        Clock fixedClock = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        Screening screening = Screening.builder()
                .startTime(now.minusMinutes(5))
                .duration(Duration.ofMinutes(60))
                .movie(Movie.builder().title("Expected Movie Title").build())
                .build();

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketId)
                .status(TicketStatus.VALID)
                .screening(screening)
                .build();

        TicketResponse ticketResponse = TicketResponse.builder()
                .seatNumbers(List.of(1))
                .movieTitle("Expected Movie Title")
                .screeningStartTime(screening.getStartTime())
                .ticketId(ticketId)
                .status(TicketStatus.USED)
                .email("example@gmail.com")
                .ticketOwner("John Doe")
                .price(DEFAULT_SEAT_PRICE)
                .build();

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.getReferenceById(ticketId)).thenReturn(ticket);
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        doReturn(ticketResponse).when(ticketValidationService).mapToTicketResponse(ticket);

        // when
        TicketResponse result = ticketValidationService.scanTicket(ticketId);

        // then
        assertThat(result.ticketId()).isEqualTo(ticketId);
        assertThat(result.seatNumbers()).isNotEmpty();
        assertThat(result.screeningStartTime()).isEqualTo(screening.getStartTime());
        assertThat(result.movieTitle()).isEqualTo("Expected Movie Title");
        assertThat(result.status()).isEqualTo(TicketStatus.USED);
        assertThat(result.email()).isEqualTo("example@gmail.com");
        assertThat(result.ticketOwner()).isEqualTo("John Doe");
        assertThat(result.price()).isEqualTo(DEFAULT_SEAT_PRICE);
    }

    @Test
    void shouldThrowWhenScanningUsedTicket() {
        // given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketId)
                .status(TicketStatus.USED)
                .screening(mock(Screening.class))
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // when / then
        assertThatThrownBy(() -> ticketValidationService.scanTicket(ticketId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot scan ticket in status: USED");
    }

    @Test
    void shouldReturnSumWhenNoExpiredScreenings() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 5, 25, 14, 0);
        Clock fixedClock = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        // 1) stub updateStatusToValid → np. 7
        when(ticketRepository.updateStatusToValid(
                eq(TicketStatus.VALID),
                eq(TicketStatus.TO_BE_CALCULATED),
                eq(now),
                eq(now.plusMinutes(15))
        )).thenReturn(7);

        // 2) screening wystartowany, ale jeszcze nie wygasł
        Screening s = Screening.builder()
                .id(42L)
                .startTime(now.minusMinutes(10))
                .duration(Duration.ofMinutes(30))
                .build();
        when(screeningRepository.findStartedScreenings(now))
                .thenReturn(List.of(s));

        // 3) stub expirePastTicketsByScreening dla pustej listy → 0
        when(ticketRepository.expirePastTicketsByScreening(List.of()))
                .thenReturn(0);

        // when
        int result = ticketValidationService.updateTicketStatus();

        // then
        assertThat(result).isEqualTo(7); // 7 + 0
        verify(ticketRepository).updateStatusToValid(
                TicketStatus.VALID,
                TicketStatus.TO_BE_CALCULATED,
                now,
                now.plusMinutes(15)
        );
        // teraz zawsze wywołujemy expirePastTicketsByScreening nawet dla pustej listy
        verify(ticketRepository).expirePastTicketsByScreening(List.of());
    }

    @Test
    void shouldReturnSumWhenThereAreExpiredScreenings() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 5, 25, 16, 0);
        Clock fixedClock = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        // 1) stub updateStatusToValid → np. 2
        when(ticketRepository.updateStatusToValid(
                any(), any(), any(), any()
        )).thenReturn(2);

        // 2) jeden seans już zakończony
        Screening expired = Screening.builder()
                .id(99L)
                .startTime(now.minusHours(2))
                .duration(Duration.ofMinutes(60))
                .build();
        when(screeningRepository.findStartedScreenings(now))
                .thenReturn(List.of(expired));

        // 3) stub expirePastTicketsByScreening dla [99] → 5
        when(ticketRepository.expirePastTicketsByScreening(List.of(99L)))
                .thenReturn(5);

        // when
        int result = ticketValidationService.updateTicketStatus();

        // then
        assertThat(result).isEqualTo(7); // 2 + 5
        verify(ticketRepository).updateStatusToValid(
                TicketStatus.VALID,
                TicketStatus.TO_BE_CALCULATED,
                now,
                now.plusMinutes(15)
        );
        verify(ticketRepository).expirePastTicketsByScreening(List.of(99L));
    }

}
