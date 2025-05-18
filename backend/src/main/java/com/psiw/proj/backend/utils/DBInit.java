package com.psiw.proj.backend.utils;

import com.psiw.proj.backend.entity.Movie;
import com.psiw.proj.backend.entity.Room;
import com.psiw.proj.backend.entity.Screening;
import com.psiw.proj.backend.entity.Seat;
import com.psiw.proj.backend.repository.MovieRepository;
import com.psiw.proj.backend.repository.RoomRepository;
import com.psiw.proj.backend.repository.ScreeningRepository;
import com.psiw.proj.backend.repository.SeatRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class DBInit {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;

    @PostConstruct
    public void init() {
        if (roomRepository.count() > 0) return; // avoid duplicates

        // 1. Create rooms with different sizes
        List<Room> rooms = roomRepository.saveAll(List.of(
                Room.builder().rowCount(5).columnCount(5).build(),
                Room.builder().rowCount(8).columnCount(10).build(),
                Room.builder().rowCount(6).columnCount(7).build(),
                Room.builder().rowCount(10).columnCount(12).build(),
                Room.builder().rowCount(4).columnCount(6).build()
        ));

        // 2. Generate all seats for each room
        List<Seat> allSeats = rooms.stream()
                .flatMap(room -> IntStream.rangeClosed(1, room.getRowCount())
                        .boxed()
                        .flatMap(row -> IntStream.rangeClosed(1, room.getColumnCount())
                                .mapToObj(col -> Seat.builder()
                                        .rowNumber(row)
                                        .columnNumber(col)
                                        .room(room)
                                        .build())))
                .toList();
        seatRepository.saveAll(allSeats);

        // 3. Create sample movies
        List<Movie> movies = movieRepository.saveAll(List.of(
                Movie.builder().title("Inception").description("Sci-fi thriller").image("inception.jpg").build(),
                Movie.builder().title("The Matrix").description("Virtual reality action").image("matrix.jpg").build(),
                Movie.builder().title("Interstellar").description("Space-time journey").image("interstellar.jpg").build(),
                Movie.builder().title("The Dark Knight").description("Gotham vigilante").image("dark_knight.jpg").build(),
                Movie.builder().title("Avatar").description("Epic science fiction").image("avatar.jpg").build(),
                Movie.builder().title("Parasite").description("Thriller from Korea").image("parasite.jpg").build()
        ));

        // 4. Generate screenings for next 3 days, two per movie per day, rotating rooms
        List<Screening> screenings = new ArrayList<>();
        LocalDate startDate = LocalDate.now().plusDays(1);
        AtomicInteger roomIndex = new AtomicInteger();

        movies.forEach(movie -> {
            for (int day = 0; day < 3; day++) {
                for (int slot = 0; slot < 2; slot++) {
                    Room room = rooms.get(roomIndex.getAndIncrement() % rooms.size());
                    LocalDateTime startTime = startDate.plusDays(day)
                            .atTime(12 + slot * 3, 0);

                    screenings.add(Screening.builder()
                            .movie(movie)
                            .room(room)
                            .startTime(startTime)
                            .duration(Duration.ofMinutes(120 + slot * 10L))
                            .build());
                }
            }
        });
        screeningRepository.saveAll(screenings);

        System.out.println("✅ Database initialized with:");
        System.out.println("- " + rooms.size() + " rooms");
        System.out.println("- " + allSeats.size() + " seats");
        System.out.println("- " + movies.size() + " movies");
        System.out.println("- " + screenings.size() + " screenings");
    }

}
