package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.Movie;
import com.psiw.proj.backend.entity.Screening;
import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.exeptions.custom.TicketNotFoundException;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.utils.TicketStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketValidationServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

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

        TicketResponse ticketResponse = new TicketResponse(
                List.of("R1C1"),
                "Expected Movie Title",
                screening.getStartTime(),
                ticketId,
                TicketStatus.USED
        );

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
}
