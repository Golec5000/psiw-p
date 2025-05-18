package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.*;
import com.psiw.proj.backend.exeptions.custom.ScreeningNotFoundException;
import com.psiw.proj.backend.repository.ScreeningRepository;
import com.psiw.proj.backend.repository.SeatRepository;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.repository.TicketSeatRepository;
import com.psiw.proj.backend.utils.TicketStatus;
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

        Room room = Room.builder()
                .roomNumber(5L)
                .build();
        Screening screening = Screening.builder()
                .id(screeningId)
                .room(room)
                .build();

        Seat seat1 = Seat.builder()
                .id(100L)
                .room(room)
                .build();

        Seat seat2 = Seat.builder()
                .id(101L)
                .room(room)
                .build();

        //when
        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.countByIdInAndRoomRoomNumber(seatIds, room.getRoomNumber())).thenReturn(2L);
        when(ticketRepository.findAllByScreeningId(screeningId)).thenReturn(List.of());

        Ticket savedTicket = Ticket.builder()
                .ticketNumber(UUID.fromString("6cb796ab-1cba-44ed-9cdf-6ca17cf9eb76"))
                .build();
        when(ticketRepository.save(any())).thenReturn(savedTicket);
        when(seatRepository.findAllById(seatIds)).thenReturn(List.of(seat1, seat2));

        Ticket result = reservationService.reserveSeats(screeningId, seatIds);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTicketSeats()).hasSize(2);
        verify(ticketSeatRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldThrowExceptionWhenScreeningNotFound() {
        // given
        when(screeningRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.reserveSeats(1L, List.of(1L)))
                .isInstanceOf(ScreeningNotFoundException.class)
                .hasMessageContaining("Screening not found");
    }

    @Test
    void shouldThrowWhenSeatsAreNotFromSameRoom() {
        // given
        Long screeningId = 2L;
        Screening screening = Screening.builder().id(screeningId).room(Room.builder().roomNumber(99L).build()).build();
        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));

        List<Long> seatIds = List.of(1L, 2L);
        when(seatRepository.countByIdInAndRoomRoomNumber(seatIds, 99L)).thenReturn(1L); // tylko 1 pasuje

        // when & then
        assertThatThrownBy(() -> reservationService.reserveSeats(screeningId, seatIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("One or more seats not found");
    }

    @Test
    void shouldThrowWhenAnySeatIsAlreadyTaken() {
        // given
        Long screeningId = 3L;
        List<Long> seatIds = List.of(10L, 11L);

        Room room = Room.builder()
                .roomNumber(1L)
                .build();

        Screening screening = Screening.builder()
                .id(screeningId)
                .room(room)
                .build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.countByIdInAndRoomRoomNumber(seatIds, room.getRoomNumber())).thenReturn(2L);

        Seat seat = Seat.builder()
                .id(10L)
                .build();

        TicketSeat ts = TicketSeat.builder()
                .seat(seat)
                .build();

        Ticket ticket = Ticket.builder()
                .ticketSeats(List.of(ts))
                .build();

        when(ticketRepository.findAllByScreeningId(screeningId)).thenReturn(List.of(ticket));

        // when & then
        assertThatThrownBy(() -> reservationService.reserveSeats(screeningId, seatIds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seats already taken");
    }

    @Test
    void shouldSaveTicketWithCorrectValues() {
        // given
        Long screeningId = 4L;
        List<Long> seatIds = List.of(101L, 102L);
        Room room = Room.builder().roomNumber(77L).build();
        Screening screening = Screening.builder()
                .id(screeningId)
                .room(room)
                .startTime(LocalDateTime.now())
                .duration(Duration.ofMinutes(100))
                .build();

        Seat seat1 = Seat.builder()
                .id(101L)
                .room(room)
                .build();

        Seat seat2 = Seat.builder()
                .id(102L)
                .room(room)
                .build();

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
        Ticket result = reservationService.reserveSeats(screeningId, seatIds);

        // then
        Ticket saved = ticketCaptor.getValue();
        assertThat(saved.getScreening()).isEqualTo(screening);
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.VALID);
        assertThat(result.getTicketSeats()).hasSize(2);
    }
}
