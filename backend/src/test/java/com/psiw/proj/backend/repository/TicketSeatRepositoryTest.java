package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.*;
import com.psiw.proj.backend.utils.TicketStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class TicketSeatRepositoryTest {

    @Autowired
    private TicketSeatRepository ticketSeatRepository;

    @Autowired
    private ScreeningRepository screeningRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldAllowSameSeatForDifferentScreenings() {
        // given
        Room room = roomRepository.save(Room.builder()
                .rowCount(10)
                .columnCount(10)
                .build());

        // zapewniamy seatNumber i seatPrice
        Seat seat = seatRepository.save(Seat.builder()
                .rowNumber(5)
                .columnNumber(6)
                .seatNumber(56)
                .seatPrice(BigDecimal.valueOf(20.0))
                .room(room)
                .build());

        Movie movie = movieRepository.save(Movie.builder()
                .title("Matrix")
                .description("Sci-fi")
                .image("matrix.jpg")
                .build());

        // Seans 1 – 18:00
        Screening screening1 = screeningRepository.save(Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0).withNano(0))
                .duration(Duration.ofMinutes(120))
                .build());

        // Seans 2 – 20:00
        Screening screening2 = screeningRepository.save(Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.now().withHour(20).withMinute(0).withSecond(0).withNano(0))
                .duration(Duration.ofMinutes(120))
                .build());

        // Rezerwacja miejsca R5C6 na seans 1
        Ticket ticket1 = ticketRepository.save(Ticket.builder()
                .screening(screening1)
                .ticketPrice(BigDecimal.valueOf(15.0))
                .ownerName("John")
                .ownerSurname("Doe")
                .ownerEmail("john.doe@example.com")
                .status(TicketStatus.VALID)
                .build());

        ticketSeatRepository.save(TicketSeat.builder()
                .screening(screening1)
                .seat(seat)
                .ticket(ticket1)
                .build());

        // Rezerwacja tego samego miejsca na seans 2
        Ticket ticket2 = ticketRepository.save(Ticket.builder()
                .screening(screening2)
                .ticketPrice(BigDecimal.valueOf(15.0))
                .ownerName("Jane")
                .ownerSurname("Smith")
                .ownerEmail("jane.smith@example.com")
                .status(TicketStatus.VALID)
                .build());

        ticketSeatRepository.save(TicketSeat.builder()
                .screening(screening2)
                .seat(seat)
                .ticket(ticket2)
                .build());

        entityManager.flush();
        entityManager.clear();

        // when
        Set<Long> takenSeatsScreening1 = ticketSeatRepository.findTakenSeatIds(screening1.getId());
        Set<Long> takenSeatsScreening2 = ticketSeatRepository.findTakenSeatIds(screening2.getId());

        // then
        assertThat(takenSeatsScreening1).containsExactly(seat.getId());
        assertThat(takenSeatsScreening2).containsExactly(seat.getId());
    }

    @Test
    void shouldReturnSeatIdsTakenForGivenScreening() {
        // given
        Room room = roomRepository.save(Room.builder()
                .rowCount(5)
                .columnCount(5)
                .build());

        Seat seat1 = seatRepository.save(Seat.builder()
                .rowNumber(1)
                .columnNumber(1)
                .seatNumber(11)
                .seatPrice(BigDecimal.valueOf(12.5))
                .room(room)
                .build());
        Seat seat2 = seatRepository.save(Seat.builder()
                .rowNumber(1)
                .columnNumber(2)
                .seatNumber(12)
                .seatPrice(BigDecimal.valueOf(12.5))
                .room(room)
                .build());

        Movie movie = movieRepository.save(Movie.builder()
                .title("Taken")
                .description("...")
                .image("poster.jpg")
                .build());

        Screening screening = screeningRepository.save(Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.now().plusDays(1))
                .duration(Duration.ofMinutes(120))
                .build());

        Ticket ticket = ticketRepository.save(Ticket.builder()
                .screening(screening)
                .ticketPrice(BigDecimal.valueOf(10.0))
                .ownerName("Alice")
                .ownerSurname("Wonderland")
                .ownerEmail("alice@example.com")
                .status(TicketStatus.VALID)
                .build());

        TicketSeat ts1 = TicketSeat.builder()
                .screening(screening)
                .seat(seat1)
                .ticket(ticket)
                .build();
        TicketSeat ts2 = TicketSeat.builder()
                .screening(screening)
                .seat(seat2)
                .ticket(ticket)
                .build();
        ticketSeatRepository.saveAll(List.of(ts1, ts2));

        entityManager.flush();
        entityManager.clear();

        // when
        Set<Long> takenSeatIds = ticketSeatRepository.findTakenSeatIds(screening.getId());

        // then
        assertThat(takenSeatIds).containsExactlyInAnyOrder(seat1.getId(), seat2.getId());
    }

    @Test
    void shouldReturnEmptySetIfNoTicketsForScreening() {
        // given
        Movie movie = movieRepository.save(Movie.builder()
                .title("Empty")
                .description("...")
                .image("img")
                .build());
        Room room = roomRepository.save(Room.builder()
                .rowCount(3)
                .columnCount(3)
                .build());

        Screening screening = screeningRepository.save(Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.now().plusDays(2))
                .duration(Duration.ofMinutes(90))
                .build());

        entityManager.flush();
        entityManager.clear();

        // when
        Set<Long> takenSeats = ticketSeatRepository.findTakenSeatIds(screening.getId());

        // then
        assertThat(takenSeats).isEmpty();
    }

    @Test
    void shouldReturnEmptySetForNonExistingScreeningId() {
        // when
        Set<Long> seatIds = ticketSeatRepository.findTakenSeatIds(9999L);

        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(seatIds).isEmpty();
    }

    @Test
    void shouldHandleMixedScreeningsGracefully() {
        // setup: create 2 screenings, one with seats, one without
        Room room = roomRepository.save(Room.builder()
                .rowCount(5)
                .columnCount(5)
                .build());
        Movie movie = movieRepository.save(Movie.builder()
                .title("Multi")
                .description("..")
                .image("x")
                .build());

        Screening s1 = screeningRepository.save(Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.now().plusDays(1))
                .duration(Duration.ofMinutes(120))
                .build());

        Screening s2 = screeningRepository.save(Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.now().plusDays(2))
                .duration(Duration.ofMinutes(100))
                .build());

        Seat seat = seatRepository.save(Seat.builder()
                .rowNumber(1)
                .columnNumber(1)
                .seatNumber(11)
                .seatPrice(BigDecimal.valueOf(15.0))
                .room(room)
                .build());
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .screening(s1)
                .ticketPrice(BigDecimal.valueOf(15.0))
                .ownerName("Bob")
                .ownerSurname("Builder")
                .ownerEmail("bob@example.com")
                .status(TicketStatus.VALID)
                .build());
        ticketSeatRepository.save(TicketSeat.builder()
                .screening(s1)
                .seat(seat)
                .ticket(ticket)
                .build());

        entityManager.flush();
        entityManager.clear();

        // when
        Set<Long> s1Taken = ticketSeatRepository.findTakenSeatIds(s1.getId());
        Set<Long> s2Taken = ticketSeatRepository.findTakenSeatIds(s2.getId());

        // then
        assertThat(s1Taken).containsExactly(seat.getId());
        assertThat(s2Taken).isEmpty();
    }

    @Test
    void shouldReturnEmptySetWhenTicketSeatsAreForOtherScreenings() {
        // given
        Room room = roomRepository.save(Room.builder()
                .rowCount(3)
                .columnCount(3)
                .build());
        Movie movie = movieRepository.save(Movie.builder()
                .title("Mismatch")
                .description("..")
                .image("a")
                .build());

        Screening targetScreening = screeningRepository.save(Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.now())
                .duration(Duration.ofMinutes(100))
                .build());

        Screening otherScreening = screeningRepository.save(Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.now().plusDays(1))
                .duration(Duration.ofMinutes(90))
                .build());

        Seat seat = seatRepository.save(Seat.builder()
                .rowNumber(1)
                .columnNumber(1)
                .seatNumber(11)
                .seatPrice(BigDecimal.valueOf(10.0))
                .room(room)
                .build());
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .screening(otherScreening)
                .ticketPrice(BigDecimal.valueOf(10.0))
                .ownerName("Carl")
                .ownerSurname("Johnson")
                .ownerEmail("cj@example.com")
                .status(TicketStatus.VALID)
                .build());

        ticketSeatRepository.save(TicketSeat.builder()
                .screening(otherScreening)
                .ticket(ticket)
                .seat(seat)
                .build());

        entityManager.flush();
        entityManager.clear();

        // when
        Set<Long> takenSeatIds = ticketSeatRepository.findTakenSeatIds(targetScreening.getId());

        // then
        assertThat(takenSeatIds).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenSameSeatAssignedToMultipleTicketsInSameScreening() {
        // given
        Room room = roomRepository.save(Room.builder()
                .rowCount(5)
                .columnCount(5)
                .build());
        Seat seat = seatRepository.save(Seat.builder()
                .rowNumber(5)
                .columnNumber(6)
                .seatNumber(56)
                .seatPrice(BigDecimal.valueOf(18.0))
                .room(room)
                .build());

        Movie movie = movieRepository.save(Movie.builder()
                .title("Duplicated Seat")
                .description("...")
                .image("dup.jpg")
                .build());

        Screening screening = screeningRepository.save(Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.now().plusDays(1))
                .duration(Duration.ofMinutes(120))
                .build());

        Ticket ticket1 = ticketRepository.save(Ticket.builder()
                .screening(screening)
                .ticketPrice(BigDecimal.valueOf(18.0))
                .ownerName("Dana")
                .ownerSurname("Scully")
                .ownerEmail("dana.scully@example.com")
                .status(TicketStatus.VALID)
                .build());
        Ticket ticket2 = ticketRepository.save(Ticket.builder()
                .screening(screening)
                .ticketPrice(BigDecimal.valueOf(18.0))
                .ownerName("Fox")
                .ownerSurname("Mulder")
                .ownerEmail("fox.mulder@example.com")
                .status(TicketStatus.VALID)
                .build());

        // zapis pierwszego przypisania miejsca
        ticketSeatRepository.save(TicketSeat.builder()
                .ticket(ticket1)
                .screening(screening)
                .seat(seat)
                .build());

        entityManager.flush();
        entityManager.clear();

        // when + then – drugie przypisanie tego samego miejsca do innego biletu w tym samym seansie
        assertThatThrownBy(() ->
                ticketSeatRepository.saveAndFlush(TicketSeat.builder()
                        .ticket(ticket2)
                        .screening(screening)
                        .seat(seat)
                        .build())
        ).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
