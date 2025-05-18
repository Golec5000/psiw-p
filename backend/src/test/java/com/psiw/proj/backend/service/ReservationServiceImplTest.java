package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.*;
import com.psiw.proj.backend.exeptions.custom.ScreeningNotFoundException;
import com.psiw.proj.backend.repository.ScreeningRepository;
import com.psiw.proj.backend.repository.SeatRepository;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.repository.TicketSeatRepository;
import com.psiw.proj.backend.utils.TicketStatus;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketSeatRepository ticketSeatRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    void shouldReserveSeatsSuccessfully() {
        // given
        Long screeningId = 1L;
        List<Long> seatIds = List.of(100L, 101L);

        Room room = Room.builder().roomNumber(5L).build();
        Movie movie = Movie.builder().title("Matrix").build();

        Screening screening = Screening.builder()
                .id(screeningId)
                .room(room)
                .movie(movie)
                .startTime(LocalDateTime.of(2025, 5, 22, 20, 0))
                .duration(Duration.ofMinutes(120))
                .build();

        Seat seat1 = Seat.builder().id(100L).rowNumber(1).columnNumber(2).room(room).build();
        Seat seat2 = Seat.builder().id(101L).rowNumber(1).columnNumber(3).room(room).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.countByIdInAndRoomRoomNumber(seatIds, room.getRoomNumber())).thenReturn(2L);
        when(ticketRepository.findAllByScreeningId(screeningId)).thenReturn(List.of());
        when(seatRepository.findAllById(seatIds)).thenReturn(List.of(seat1, seat2));
        when(ticketRepository.save(any())).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setTicketNumber(UUID.randomUUID());
            return t;
        });

        // when
        TicketResponse response = reservationService.reserveSeats(screeningId, seatIds);

        // then
        assertThat(response).isNotNull();
        assertThat(response.movieTitle()).isEqualTo("Matrix");
        assertThat(response.screeningStartTime()).isEqualTo(screening.getStartTime());
        assertThat(response.seatNumbers()).containsExactlyInAnyOrder("R1C2", "R1C3");
        verify(ticketSeatRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldThrowExceptionWhenScreeningNotFound() {
        when(screeningRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.reserveSeats(1L, List.of(1L)))
                .isInstanceOf(ScreeningNotFoundException.class)
                .hasMessageContaining("Screening not found");
    }

    @Test
    void shouldThrowWhenSeatsAreNotFromSameRoom() {
        Long screeningId = 2L;
        Room room = Room.builder().roomNumber(99L).build();
        Screening screening = Screening.builder().id(screeningId).room(room).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.countByIdInAndRoomRoomNumber(List.of(1L, 2L), 99L)).thenReturn(1L); // tylko 1 pasuje

        assertThatThrownBy(() -> reservationService.reserveSeats(screeningId, List.of(1L, 2L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("One or more seats not found");
    }

    @Test
    void shouldThrowWhenAnySeatIsAlreadyTaken() {
        Long screeningId = 3L;
        List<Long> seatIds = List.of(10L, 11L);

        Room room = Room.builder().roomNumber(1L).build();
        Screening screening = Screening.builder().id(screeningId).room(room).build();

        Seat seat = Seat.builder().id(10L).build();
        TicketSeat ts = TicketSeat.builder().seat(seat).build();
        Ticket ticket = Ticket.builder().ticketSeats(List.of(ts)).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.countByIdInAndRoomRoomNumber(seatIds, room.getRoomNumber())).thenReturn(2L);
        when(ticketRepository.findAllByScreeningId(screeningId)).thenReturn(List.of(ticket));

        assertThatThrownBy(() -> reservationService.reserveSeats(screeningId, seatIds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seats already taken");
    }

    @Test
    void shouldSaveTicketWithCorrectValues() {
        Long screeningId = 4L;
        List<Long> seatIds = List.of(101L, 102L);
        Room room = Room.builder().roomNumber(77L).build();
        Movie movie = Movie.builder().title("John Wick").build();
        Screening screening = Screening.builder()
                .id(screeningId)
                .room(room)
                .movie(movie)
                .startTime(LocalDateTime.of(2025, 6, 1, 18, 30))
                .duration(Duration.ofMinutes(110))
                .build();

        Seat seat1 = Seat.builder().id(101L).rowNumber(2).columnNumber(1).room(room).build();
        Seat seat2 = Seat.builder().id(102L).rowNumber(2).columnNumber(2).room(room).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.countByIdInAndRoomRoomNumber(seatIds, room.getRoomNumber())).thenReturn(2L);
        when(ticketRepository.findAllByScreeningId(screeningId)).thenReturn(List.of());
        when(seatRepository.findAllById(seatIds)).thenReturn(List.of(seat1, seat2));

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        when(ticketRepository.save(ticketCaptor.capture())).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setTicketNumber(UUID.randomUUID());
            return t;
        });

        // when
        TicketResponse response = reservationService.reserveSeats(screeningId, seatIds);

        // then
        Ticket saved = ticketCaptor.getValue();
        assertThat(saved.getScreening()).isEqualTo(screening);
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.VALID);
        assertThat(response.seatNumbers()).containsExactlyInAnyOrder("R2C1", "R2C2");
        assertThat(response.movieTitle()).isEqualTo("John Wick");
        assertThat(response.screeningStartTime()).isEqualTo(screening.getStartTime());
    }
}
