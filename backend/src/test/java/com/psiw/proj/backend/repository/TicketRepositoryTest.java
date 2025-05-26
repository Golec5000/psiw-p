package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.*;
import com.psiw.proj.backend.utils.enums.TicketStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldLoadTicketsWithSeatsForGivenScreening() {
        // given
        Room room = Room.builder().roomNumber("A1").rowCount(5).columnCount(5).build();
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
        Room room = Room.builder().roomNumber("E1").rowCount(3).columnCount(3).build();
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

    @Test
    void updateStatusToValid_shouldOnlyTouchTicketsWithinNext15Minutes() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // seans za 10 minut → kwalifikuje się
        Screening soon = createAndPersistScreening(now.plusMinutes(10), Duration.ofMinutes(60));
        Ticket t1 = createAndPersistTicket(soon, TicketStatus.TO_BE_CALCULATED);

        // seans za 20 minut → poza 15-min oknem
        Screening later = createAndPersistScreening(now.plusMinutes(20), Duration.ofMinutes(60));
        Ticket t2 = createAndPersistTicket(later, TicketStatus.TO_BE_CALCULATED);

        // seans za 5 minut, ale już VALID → nie powinno się zmienić
        Screening soonValid = createAndPersistScreening(now.plusMinutes(5), Duration.ofMinutes(60));
        Ticket t3 = createAndPersistTicket(soonValid, TicketStatus.VALID);

        entityManager.flush();
        entityManager.clear();

        // when
        int updated = ticketRepository.updateStatusToValid(
                TicketStatus.VALID,
                TicketStatus.TO_BE_CALCULATED,
                now,
                now.plusMinutes(15)
        );

        entityManager.clear();

        // then
        assertThat(updated).isEqualTo(1);

        Ticket fetched1 = ticketRepository.findById(t1.getTicketNumber()).get();
        Ticket fetched2 = ticketRepository.findById(t2.getTicketNumber()).get();
        Ticket fetched3 = ticketRepository.findById(t3.getTicketNumber()).get();

        assertThat(fetched1.getStatus()).isEqualTo(TicketStatus.VALID);
        assertThat(fetched2.getStatus()).isEqualTo(TicketStatus.TO_BE_CALCULATED);
        assertThat(fetched3.getStatus()).isEqualTo(TicketStatus.VALID);
    }

    @Test
    void expirePastTicketsByScreening_shouldExpireOnlyGivenScreenings() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // skonstruuj 2 seansów: jeden już zakończony, drugi dopiero w przyszłości
        Screening past = createAndPersistScreening(now.minusHours(2), Duration.ofMinutes(30));
        Ticket pValid = createAndPersistTicket(past, TicketStatus.VALID);
        Ticket pToCalc = createAndPersistTicket(past, TicketStatus.TO_BE_CALCULATED);
        Ticket pUsed = createAndPersistTicket(past, TicketStatus.USED); // nie powinien się zmienić

        Screening future = createAndPersistScreening(now.plusMinutes(10), Duration.ofMinutes(30));
        Ticket fValid = createAndPersistTicket(future, TicketStatus.VALID);
        Ticket fToCalc = createAndPersistTicket(future, TicketStatus.TO_BE_CALCULATED);

        entityManager.flush();
        entityManager.clear();

        // kiedy wywołamy
        int count = ticketRepository.expirePastTicketsByScreening(List.of(past.getId()));
        entityManager.clear();

        // tylko dwa bilety z przeszłego seansu powinny zmienić status
        assertThat(count).isEqualTo(2);

        Ticket fetchedPValid = ticketRepository.findById(pValid.getTicketNumber()).get();
        Ticket fetchedPToCalc = ticketRepository.findById(pToCalc.getTicketNumber()).get();
        Ticket fetchedPUsed = ticketRepository.findById(pUsed.getTicketNumber()).get();
        Ticket fetchedFValid = ticketRepository.findById(fValid.getTicketNumber()).get();
        Ticket fetchedFToCalc = ticketRepository.findById(fToCalc.getTicketNumber()).get();

        assertThat(fetchedPValid.getStatus()).isEqualTo(TicketStatus.EXPIRED);
        assertThat(fetchedPToCalc.getStatus()).isEqualTo(TicketStatus.EXPIRED);
        assertThat(fetchedPUsed.getStatus()).isEqualTo(TicketStatus.USED);                // niezmieniony
        assertThat(fetchedFValid.getStatus()).isEqualTo(TicketStatus.VALID);
        assertThat(fetchedFToCalc.getStatus()).isEqualTo(TicketStatus.TO_BE_CALCULATED);
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

    private Screening createAndPersistScreening(LocalDateTime start, Duration duration) {
        // generujemy unikalną nazwę pokoju
        String roomNumber = "R-" + System.nanoTime();

        Room room = Room.builder()
                .roomNumber(roomNumber)
                .rowCount(3)
                .columnCount(3)
                .build();
        entityManager.persist(room);

        String uniqueTitle = "TestMovie" + System.nanoTime();
        Movie movie = Movie.builder()
                .title(uniqueTitle)
                .description("Desc")
                .image("img")
                .build();
        entityManager.persist(movie);

        Screening s = Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(start)
                .duration(duration)
                .build();
        entityManager.persist(s);
        return s;
    }


    private Ticket createAndPersistTicket(Screening screening, TicketStatus status) {
        Ticket t = Ticket.builder()
                .screening(screening)
                .status(status)
                .ownerEmail("a@b.com")
                .ownerName("X")
                .ownerSurname("Y")
                .ticketPrice(BigDecimal.ONE)
                .build();
        entityManager.persist(t);
        return t;
    }
}
