package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.Movie;
import com.psiw.proj.backend.entity.Room;
import com.psiw.proj.backend.entity.Screening;
import com.psiw.proj.backend.entity.Seat;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class ScreeningRepositoryTest {

    @Autowired
    private ScreeningRepository screeningRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldFindScreeningWithRoomMovieAndSeats() {
        // given
        Room room = Room.builder()
                .rowCount(5)
                .columnCount(5)
                .build();
        entityManager.persist(room);

        // dodajemy kilka miejsc
        for (int row = 1; row <= 2; row++) {
            for (int col = 1; col <= 2; col++) {
                Seat seat = Seat.builder()
                        .rowNumber(row)
                        .columnNumber(col)
                        .room(room)
                        .seatPrice(BigDecimal.TEN)
                        .seatNumber(row + col)
                        .build();
                room.getSeats().add(seat);
                entityManager.persist(seat);
            }
        }

        Movie movie = Movie.builder()
                .title("EntityGraph Movie")
                .description("With seats")
                .image("img.jpg")
                .build();
        entityManager.persist(movie);

        Screening screening = Screening.builder()
                .startTime(LocalDateTime.now().plusDays(1))
                .duration(Duration.ofMinutes(100))
                .room(room)
                .movie(movie)
                .build();

        entityManager.persist(screening);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Screening> result = screeningRepository.findByIdWithRoomAndMovie(screening.getId());

        // then
        assertThat(result).isPresent();
        Screening fetched = result.get();

        assertThat(fetched.getMovie()).isNotNull();
        assertThat(fetched.getMovie().getTitle()).isEqualTo("EntityGraph Movie");

        assertThat(fetched.getRoom()).isNotNull();
        assertThat(fetched.getRoom().getSeats()).hasSize(4); // 2x2 = 4 miejsca
    }

    @Test
    void shouldReturnEmptyIfScreeningDoesNotExist() {
        // when
        Optional<Screening> result = screeningRepository.findByIdWithRoomAndMovie(999L);

        // then
        assertThat(result).isEmpty();
    }
}
