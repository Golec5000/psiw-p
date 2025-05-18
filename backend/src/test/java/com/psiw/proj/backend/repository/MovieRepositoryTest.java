package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.Movie;
import com.psiw.proj.backend.entity.Room;
import com.psiw.proj.backend.entity.Screening;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldLoadMoviesWithScreeningsAndRoomsEagerlyUsingEntityGraph() {
        // given
        LocalDate targetDate = LocalDate.now().plusDays(1);
        LocalDateTime from = targetDate.atStartOfDay();
        LocalDateTime to = targetDate.plusDays(1).atStartOfDay();

        // tworzę salę
        Room room = Room.builder()
                .rowCount(10)
                .columnCount(10)
                .build();

        // tworzę film
        Movie movie = Movie.builder()
                .title("Test Movie")
                .description("Test Description")
                .image("test.jpg")
                .build();

        // create and persist independent entities first
        entityManager.persist(room);
        entityManager.persist(movie);
        entityManager.flush(); // ensure IDs are generated

        // create screening after room and movie are persisted
        Screening screening = Screening.builder()
                .startTime(from.plusHours(2))
                .duration(Duration.ofMinutes(120))
                .room(room)
                .movie(movie)
                .build();

        // build bidirectional relationships
        movie.getScreenings().add(screening);
        room.getScreenings().add(screening);

        // persist dependent entity
        entityManager.persist(screening);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Movie> movies = movieRepository.findDistinctByScreeningsStartTimeBetween(from, to);

        // then
        assertThat(movies).isNotEmpty();
        for (Movie m : movies) {
            assertThat(m.getScreenings()).isNotEmpty();
            for (Screening s : m.getScreenings()) {
                assertThat(s.getRoom()).isNotNull(); // room powinien być eagerly załadowany
                assertThat(s.getStartTime()).isAfterOrEqualTo(from);
                assertThat(s.getStartTime()).isBefore(to);
            }
        }
    }

    @Test
    void shouldNotReturnMoviesWithoutScreeningsInRange() {
        // given
        LocalDate targetDate = LocalDate.now().plusDays(1);
        LocalDateTime from = targetDate.atStartOfDay();
        LocalDateTime to = targetDate.plusDays(1).atStartOfDay();

        Room room = Room.builder().rowCount(5).columnCount(5).build();
        Movie movie = Movie.builder()
                .title("Out of Range Movie")
                .description("This won't match")
                .image("image.jpg")
                .build();

        entityManager.persist(room);
        entityManager.persist(movie);
        entityManager.flush();

        Screening screening = Screening.builder()
                .startTime(from.minusDays(2)) // poza zakresem
                .duration(Duration.ofMinutes(90))
                .room(room)
                .movie(movie)
                .build();

        movie.getScreenings().add(screening);
        room.getScreenings().add(screening);

        entityManager.persist(screening);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Movie> result = movieRepository.findDistinctByScreeningsStartTimeBetween(from, to);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnMovieWithOnlySomeScreeningsInRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.plusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime to = from.plusDays(1);

        Room room = Room.builder().rowCount(10).columnCount(10).build();
        Movie movie = Movie.builder()
                .title("Partial Range Movie")
                .description("Only one screening matches")
                .image("image.jpg")
                .build();

        entityManager.persist(room);
        entityManager.persist(movie);
        entityManager.flush();

        Screening s1 = Screening.builder()
                .startTime(from.plusHours(3)) // w zakresie
                .duration(Duration.ofMinutes(120))
                .room(room)
                .movie(movie)
                .build();

        Screening s2 = Screening.builder()
                .startTime(now.minusDays(2)) // poza zakresem
                .duration(Duration.ofMinutes(90))
                .room(room)
                .movie(movie)
                .build();

        movie.getScreenings().add(s1);
        movie.getScreenings().add(s2);
        room.getScreenings().add(s1);
        room.getScreenings().add(s2);

        entityManager.persist(s1);
        entityManager.persist(s2);
        entityManager.flush();
        entityManager.clear();

        List<Movie> result = movieRepository.findDistinctByScreeningsStartTimeBetween(from, to);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Partial Range Movie");
    }

    @Test
    void shouldReturnMultipleMoviesWithScreeningsInRange() {
        LocalDateTime from = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime to = from.plusDays(1);

        Room room = Room.builder().rowCount(5).columnCount(5).build();
        entityManager.persist(room);

        for (int i = 1; i <= 3; i++) {
            Movie movie = Movie.builder()
                    .title("Movie " + i)
                    .description("Desc " + i)
                    .image("img" + i + ".jpg")
                    .build();

            entityManager.persist(movie);
            entityManager.flush();

            Screening screening = Screening.builder()
                    .startTime(from.plusHours(i)) // wszystkie w zakresie
                    .duration(Duration.ofMinutes(100 + i))
                    .room(room)
                    .movie(movie)
                    .build();

            movie.getScreenings().add(screening);
            room.getScreenings().add(screening);

            entityManager.persist(screening);
        }

        entityManager.flush();
        entityManager.clear();

        List<Movie> movies = movieRepository.findDistinctByScreeningsStartTimeBetween(from, to);
        assertThat(movies).hasSize(3);
    }

    @Test
    void shouldNotReturnMovieWithoutAnyScreenings() {
        Movie movie = Movie.builder()
                .title("No Screening Movie")
                .description("No screenings at all")
                .image("no.jpg")
                .build();

        entityManager.persist(movie);
        entityManager.flush();
        entityManager.clear();

        LocalDateTime from = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime to = from.plusDays(1);

        List<Movie> movies = movieRepository.findDistinctByScreeningsStartTimeBetween(from, to);
        assertThat(movies).isEmpty();
    }
}
