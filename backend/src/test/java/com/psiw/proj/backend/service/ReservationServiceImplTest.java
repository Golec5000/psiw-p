package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.*;
import com.psiw.proj.backend.exeptions.custom.ScreeningNotFoundException;
import com.psiw.proj.backend.repository.ScreeningRepository;
import com.psiw.proj.backend.repository.SeatRepository;
import com.psiw.proj.backend.repository.TicketRepository;
import com.psiw.proj.backend.repository.TicketSeatRepository;
import com.psiw.proj.backend.utils.enums.TicketStatus;
import com.psiw.proj.backend.utils.requestDto.ReservationRequest;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.psiw.proj.backend.utils.DBInit.DEFAULT_SEAT_PRICE;
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
        String email = "test@example.com";
        String name = "John";
        String surname = "Doe";

        Room room = Room.builder().roomNumber(5L).build();
        Movie movie = Movie.builder().title("Matrix").build();
        Screening screening = Screening.builder()
                .id(screeningId)
                .room(room)
                .movie(movie)
                .startTime(LocalDateTime.of(2025, 5, 22, 20, 0))
                .duration(Duration.ofMinutes(120))
                .build();

        Seat seat1 = Seat.builder()
                .id(100L)
                .seatNumber(100)
                .rowNumber(1)
                .columnNumber(2)
                .room(room)
                .build();
        Seat seat2 = Seat.builder()
                .id(101L)
                .seatNumber(101)
                .rowNumber(1)
                .columnNumber(3)
                .room(room)
                .build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.countByIdInAndRoomRoomNumber(seatIds, 5L)).thenReturn(2L);
        when(ticketRepository.findAllByScreeningId(screeningId)).thenReturn(List.of());
        when(seatRepository.findAllById(seatIds)).thenReturn(List.of(seat1, seat2));
        when(ticketRepository.save(any())).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setTicketNumber(UUID.randomUUID());
            return t;
        });

        // when
        ReservationRequest request = new ReservationRequest(screeningId, seatIds, email, name, surname);
        TicketResponse response = reservationService.reserveSeats(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.movieTitle()).isEqualTo("Matrix");
        assertThat(response.screeningStartTime()).isEqualTo(screening.getStartTime());
        assertThat(response.seatNumbers()).containsExactlyInAnyOrder(100, 101);
        assertThat(response.status()).isEqualTo(TicketStatus.VALID);
        assertThat(response.ticketId()).isNotNull();
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.ticket_owner()).isEqualTo(name + " " + surname);
        assertThat(response.price()).isEqualTo(DEFAULT_SEAT_PRICE
                .multiply(new BigDecimal(request.seatIds().size()))
                .setScale(2, RoundingMode.HALF_UP));
        verify(ticketSeatRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldThrowExceptionWhenScreeningNotFound() {
        when(screeningRepository.findById(1L)).thenReturn(Optional.empty());

        ReservationRequest request = new ReservationRequest(1L, List.of(1L), "a@b.com", "A", "B");
        assertThatThrownBy(() -> reservationService.reserveSeats(request))
                .isInstanceOf(ScreeningNotFoundException.class)
                .hasMessageContaining("Screening not found");
    }

    @Test
    void shouldThrowWhenSeatsAreNotFromSameRoom() {
        Long screeningId = 2L;
        Room room = Room.builder().roomNumber(99L).build();
        Screening screening = Screening.builder().id(screeningId).room(room).build();
        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.countByIdInAndRoomRoomNumber(List.of(1L, 2L), 99L)).thenReturn(1L);

        ReservationRequest request = new ReservationRequest(screeningId, List.of(1L, 2L), "a@b.com", "A", "B");
        assertThatThrownBy(() -> reservationService.reserveSeats(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("One or more seats");
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
        when(seatRepository.countByIdInAndRoomRoomNumber(seatIds, 1L)).thenReturn(2L);
        when(ticketRepository.findAllByScreeningId(screeningId)).thenReturn(List.of(ticket));

        ReservationRequest request = new ReservationRequest(screeningId, seatIds, "a@b.com", "A", "B");
        assertThatThrownBy(() -> reservationService.reserveSeats(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seats already taken");
    }

    @Test
    void shouldSaveTicketWithCorrectValues() {
        Long screeningId = 4L;
        List<Long> seatIds = List.of(101L, 102L);
        String email = "foo@bar.com";
        String name = "Foo";
        String surname = "Bar";

        Room room = Room.builder().roomNumber(77L).build();
        Movie movie = Movie.builder().title("John Wick").build();
        Screening screening = Screening.builder()
                .id(screeningId)
                .room(room)
                .movie(movie)
                .startTime(LocalDateTime.of(2025, 6, 1, 18, 30))
                .duration(Duration.ofMinutes(110))
                .build();

        Seat seat1 = Seat.builder()
                .id(101L)
                .seatNumber(201)
                .rowNumber(2)
                .columnNumber(1)
                .room(room)
                .build();
        Seat seat2 = Seat.builder()
                .id(102L)
                .seatNumber(202)
                .rowNumber(2)
                .columnNumber(2)
                .room(room)
                .build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.countByIdInAndRoomRoomNumber(seatIds, 77L)).thenReturn(2L);
        when(ticketRepository.findAllByScreeningId(screeningId)).thenReturn(List.of());
        when(seatRepository.findAllById(seatIds)).thenReturn(List.of(seat1, seat2));

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        when(ticketRepository.save(ticketCaptor.capture())).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setTicketNumber(UUID.randomUUID());
            return t;
        });

        ReservationRequest request = new ReservationRequest(screeningId, seatIds, email, name, surname);
        TicketResponse response = reservationService.reserveSeats(request);

        Ticket saved = ticketCaptor.getValue();
        assertThat(saved.getScreening()).isEqualTo(screening);
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.VALID);

        assertThat(response.seatNumbers()).containsExactlyInAnyOrder(201, 202);
        assertThat(response.movieTitle()).isEqualTo("John Wick");
        assertThat(response.screeningStartTime()).isEqualTo(screening.getStartTime());
        assertThat(response.status()).isEqualTo(TicketStatus.VALID);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.ticket_owner()).isEqualTo(name + " " + surname);
        assertThat(response.price()).isEqualTo(DEFAULT_SEAT_PRICE
                .multiply(new BigDecimal(request.seatIds().size()))
                .setScale(2, RoundingMode.HALF_UP));
    }
}
