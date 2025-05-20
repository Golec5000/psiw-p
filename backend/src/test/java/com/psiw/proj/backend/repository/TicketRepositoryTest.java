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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldLoadTicketsWithSeatsForGivenScreening() {
        // given
        Room room = Room.builder().rowCount(5).columnCount(5).build();
        entityManager.persist(room);

        Seat seat1 = createSeat(room, 1, 1);
        Seat seat2 = createSeat(room, 2, 2);
        entityManager.persist(seat1);
        entityManager.persist(seat2);

        Movie movie = Movie.builder()
                .title("Film")
                .description("Opis")
                .image("img.jpg")
                .build();
        entityManager.persist(movie);

        Screening screening = Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.now().plusHours(2))
                .duration(Duration.ofMinutes(90))
                .build();
        entityManager.persist(screening);

        Ticket ticket = Ticket.builder()
                .screening(screening)
                .status(TicketStatus.VALID)
                .ownerEmail("test@gmail.com")
                .ownerName("John")
                .ownerSurname("Doe")
                .ticketPrice(BigDecimal.TEN)
                .build();
        entityManager.persist(ticket);

        TicketSeat ts1 = createTicketSeat(ticket, seat1, screening);
        TicketSeat ts2 = createTicketSeat(ticket, seat2, screening);
        entityManager.persist(ts1);
        entityManager.persist(ts2);

        ticket.getTicketSeats().addAll(List.of(ts1, ts2));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Ticket> tickets = ticketRepository.findAllByScreeningId(screening.getId());

        // then
        assertThat(tickets).hasSize(1);
        Ticket fetched = tickets.getFirst();
        assertThat(fetched.getTicketSeats()).hasSize(2);
        for (TicketSeat ts : fetched.getTicketSeats()) {
            assertThat(ts.getSeat()).isNotNull();
            assertThat(ts.getSeat().getRowNumber()).isGreaterThan(0); // coś tam ma
        }
    }

    @Test
    void shouldReturnEmptyListWhenNoTicketsExistForScreening() {
        // given
        Room room = Room.builder().rowCount(3).columnCount(3).build();
        entityManager.persist(room);

        Movie movie = Movie.builder()
                .title("Another film")
                .description("desc")
                .image("img2.jpg")
                .build();
        entityManager.persist(movie);

        Screening screening = Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(LocalDateTime.now().plusDays(1))
                .duration(Duration.ofMinutes(60))
                .build();
        entityManager.persist(screening);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Ticket> tickets = ticketRepository.findAllByScreeningId(screening.getId());

        // then
        assertThat(tickets).isEmpty();
    }

    private Seat createSeat(Room room, int col, int seatNumber) {
        return Seat.builder()
                .room(room)
                .rowNumber(1)
                .columnNumber(col)
                .seatNumber(seatNumber)
                .seatPrice(BigDecimal.TEN)
                .build();
    }

    private TicketSeat createTicketSeat(Ticket ticket, Seat seat, Screening screening) {
        return TicketSeat.builder()
                .ticket(ticket)
                .seat(seat)
                .screening(screening)
                .build();
    }
}
