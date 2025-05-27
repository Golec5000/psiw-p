package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.Movie;
import com.psiw.proj.backend.entity.Room;
import com.psiw.proj.backend.entity.Screening;
import com.psiw.proj.backend.entity.Seat;
import com.psiw.proj.backend.exceptions.custom.RoomHasNoSeatsException;
import com.psiw.proj.backend.exceptions.custom.ScreeningNotFoundException;
import com.psiw.proj.backend.repository.MovieRepository;
import com.psiw.proj.backend.repository.ScreeningRepository;
import com.psiw.proj.backend.repository.TicketSeatRepository;
import com.psiw.proj.backend.service.implementation.RepertoireServiceImpl;
import com.psiw.proj.backend.utils.responseDto.MovieResponse;
import com.psiw.proj.backend.utils.responseDto.ScreeningDetailsResponse;
import com.psiw.proj.backend.utils.responseDto.helpers.SeatDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepertoireServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private TicketSeatRepository ticketSeatRepository;

    @InjectMocks
    private RepertoireServiceImpl repertoireService;

    @Test
    void shouldReturnMoviesWithScreeningsForDate() {
        // given
        LocalDate date = LocalDate.of(2025, 5, 18);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        Room room = Room.builder()
                .roomNumber("A1")
                .rowCount(5)
                .columnCount(5)
                .build();

        Movie movie = Movie.builder()
                .id(10L)
                .title("Test Movie")
                .description("Some description")
                .image("image.png")
                .build();

        Screening screening = Screening.builder()
                .id(1L)
                .startTime(from.plusHours(10))
                .duration(Duration.ofMinutes(120))
                .movie(movie)
                .room(room)
                .build();

        movie.setScreenings(List.of(screening));

        when(movieRepository.findDistinctByScreeningsStartTimeBetween(from, to)).thenReturn(List.of(movie));

        // when
        List<MovieResponse> result = repertoireService.getMoviesWithScreeningsForDate(date);

        // then
        assertThat(result).hasSize(1);
        MovieResponse response = result.getFirst();
        assertThat(response.title()).isEqualTo("Test Movie");
        assertThat(response.screenings()).hasSize(1);
        assertThat(response.screenings().getFirst().startTime()).isEqualTo(screening.getStartTime());
        assertThat(response.screenings().getFirst().duration()).isEqualTo(screening.getDuration().toMinutes());
    }

    @Test
    void shouldReturnScreeningDetails() {
        // given
        Long screeningId = 1L;

        Room room = Room.builder()
                .roomNumber("A1")
                .rowCount(3)
                .columnCount(3)
                .build();

        Seat seat1 = Seat.builder()
                .id(1L)
                .rowNumber(1)
                .columnNumber(1)
                .room(room)
                .build();

        Seat seat2 = Seat.builder()
                .id(2L)
                .rowNumber(1)
                .columnNumber(2)
                .room(room)
                .build();

        room.setSeats(List.of(seat1, seat2));

        Movie movie = Movie.builder()
                .id(5L)
                .title("Avengers")
                .description("Heroes unite")
                .image("avengers.png")
                .build();

        Screening screening = Screening.builder()
                .id(screeningId)
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.of(2025, 5, 20, 15, 0))
                .duration(Duration.ofMinutes(140))
                .build();

        when(screeningRepository.findByIdWithRoomAndMovie(screeningId)).thenReturn(Optional.of(screening));
        when(ticketSeatRepository.findTakenSeatIds(screeningId)).thenReturn(Set.of(1L)); // seat1 is taken

        // when
        ScreeningDetailsResponse result = repertoireService.getScreeningDetails(screeningId);

        // then
        assertThat(result.id()).isEqualTo(screeningId);
        assertThat(result.movie().title()).isEqualTo("Avengers");
        assertThat(result.room().roomNumber()).isEqualTo("A1");
        assertThat(result.seats()).hasSize(2);

        var seatDto1 = result.seats().stream()
                .filter(s -> s.id() == 1L)
                .findFirst()
                .orElseThrow();
        var seatDto2 = result.seats().stream()
                .filter(s -> s.id() == 2L)
                .findFirst()
                .orElseThrow();

        assertThat(seatDto1.available()).isFalse();
        assertThat(seatDto2.available()).isTrue();
    }

    @Test
    void shouldThrowExceptionIfScreeningNotFound() {
        // given
        Long screeningId = 99L;
        when(screeningRepository.findByIdWithRoomAndMovie(screeningId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> repertoireService.getScreeningDetails(screeningId))
                .isInstanceOf(ScreeningNotFoundException.class)
                .hasMessageContaining("Screening not found");
    }

    @Test
    void shouldReturnEmptyListWhenNoMoviesForDate() {
        // given
        LocalDate date = LocalDate.of(2025, 1, 1);
        when(movieRepository.findDistinctByScreeningsStartTimeBetween(any(), any())).thenReturn(List.of());

        // when
        List<MovieResponse> result = repertoireService.getMoviesWithScreeningsForDate(date);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnMovieWithNoScreeningsInDate() {
        // given
        LocalDate date = LocalDate.of(2025, 5, 18);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        Movie movie = Movie.builder()
                .id(1L)
                .title("No Show")
                .description("No screenings today")
                .image("img.png")
                .screenings(List.of()) // brak seansów
                .build();

        when(movieRepository.findDistinctByScreeningsStartTimeBetween(from, to)).thenReturn(List.of(movie));

        // when
        List<MovieResponse> result = repertoireService.getMoviesWithScreeningsForDate(date);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().screenings()).isEmpty();
    }

    @Test
    void shouldReturnMovieWithMultipleScreenings() {
        // given
        LocalDate date = LocalDate.of(2025, 5, 18);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        Movie movie = Movie.builder()
                .id(2L)
                .title("Multiple Shows")
                .description("Twice a day")
                .image("multi.png")
                .build();

        Screening s1 = Screening.builder()
                .id(101L)
                .movie(movie)
                .startTime(from.plusHours(10))
                .duration(Duration.ofMinutes(100))
                .room(Room.builder().roomNumber("A1").build())
                .build();

        Screening s2 = Screening.builder()
                .id(102L)
                .movie(movie)
                .startTime(from.plusHours(16))
                .duration(Duration.ofMinutes(100))
                .room(Room.builder().roomNumber("A1").build())
                .build();

        movie.setScreenings(List.of(s1, s2));

        when(movieRepository.findDistinctByScreeningsStartTimeBetween(from, to)).thenReturn(List.of(movie));

        // when
        List<MovieResponse> result = repertoireService.getMoviesWithScreeningsForDate(date);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().screenings()).hasSize(2);
    }

    @Test
    void shouldReturnAllSeatsAsAvailable() {
        // given
        Long screeningId = 2L;

        Room room = Room.builder()
                .roomNumber("A1")
                .rowCount(2)
                .columnCount(2)
                .build();

        Seat s1 = Seat.builder()
                .id(1L)
                .rowNumber(1)
                .columnNumber(1)
                .room(room)
                .build();

        Seat s2 = Seat.builder()
                .id(2L)
                .rowNumber(1)
                .columnNumber(2)
                .room(room)
                .build();

        room.setSeats(List.of(s1, s2));

        Movie movie = Movie.builder()
                .id(99L)
                .title("Open House")
                .image("open.png")
                .description("Plenty of room")
                .build();

        Screening screening = Screening.builder()
                .id(screeningId)
                .room(room)
                .movie(movie)
                .duration(Duration.ofMinutes(90))
                .startTime(LocalDateTime.now())
                .build();

        when(screeningRepository.findByIdWithRoomAndMovie(screeningId)).thenReturn(Optional.of(screening));
        when(ticketSeatRepository.findTakenSeatIds(screeningId)).thenReturn(Set.of()); // nic nie zajęte

        // when
        ScreeningDetailsResponse response = repertoireService.getScreeningDetails(screeningId);

        // then
        assertThat(response.seats()).allMatch(SeatDto::available);
    }

    @Test
    void shouldReturnAllSeatsAsTaken() {
        // given
        Long screeningId = 3L;

        Room room = Room.builder()
                .roomNumber("A1")
                .rowCount(1)
                .columnCount(2)
                .build();

        Seat s1 = Seat.builder()
                .id(10L)
                .rowNumber(1)
                .columnNumber(1)
                .room(room)
                .build();

        Seat s2 = Seat.builder()
                .id(11L)
                .rowNumber(1)
                .columnNumber(2)
                .room(room)
                .build();

        room.setSeats(List.of(s1, s2));

        Movie movie = Movie.builder()
                .id(3L)
                .title("Full House")
                .image("full.png")
                .description("No seats left")
                .build();

        Screening screening = Screening.builder()
                .id(screeningId)
                .room(room)
                .movie(movie)
                .duration(Duration.ofMinutes(120))
                .startTime(LocalDateTime.now())
                .build();

        when(screeningRepository.findByIdWithRoomAndMovie(screeningId)).thenReturn(Optional.of(screening));
        when(ticketSeatRepository.findTakenSeatIds(screeningId)).thenReturn(Set.of(10L, 11L));

        // when
        ScreeningDetailsResponse result = repertoireService.getScreeningDetails(screeningId);

        // then
        assertThat(result.seats()).allMatch(s -> !s.available());
    }

    @Test
    void shouldHandleNullSeatsGracefully() {
        // given
        Long screeningId = 4L;
        Room room = Room.builder().roomNumber("A1").rowCount(1).columnCount(1).seats(null).build();

        Movie movie = Movie.builder().id(4L).title("Null Seat Movie").build();

        Screening screening = Screening.builder()
                .id(screeningId)
                .movie(movie)
                .room(room)
                .duration(Duration.ofMinutes(90))
                .startTime(LocalDateTime.now())
                .build();

        when(screeningRepository.findByIdWithRoomAndMovie(screeningId)).thenReturn(Optional.of(screening));
        when(ticketSeatRepository.findTakenSeatIds(screeningId)).thenReturn(Set.of());

        // when / then
        assertThatThrownBy(() -> repertoireService.getScreeningDetails(screeningId))
                .isInstanceOf(RoomHasNoSeatsException.class)
                .hasMessageContaining("Room has no seats defined");
    }

    @Test
    void shouldHandleEmptySeatsGracefully() {
        // given
        Long screeningId = 5L;
        Room room = Room.builder().roomNumber("A1").rowCount(1).columnCount(1).seats(List.of()).build();

        Movie movie = Movie.builder().id(5L).title("Empty Room").build();

        Screening screening = Screening.builder()
                .id(screeningId)
                .movie(movie)
                .room(room)
                .duration(Duration.ofMinutes(90))
                .startTime(LocalDateTime.now())
                .build();

        when(screeningRepository.findByIdWithRoomAndMovie(screeningId)).thenReturn(Optional.of(screening));
        when(ticketSeatRepository.findTakenSeatIds(screeningId)).thenReturn(Set.of());

        // when
        ScreeningDetailsResponse result = repertoireService.getScreeningDetails(screeningId);

        // then
        assertThat(result.seats()).isEmpty();
    }
}
