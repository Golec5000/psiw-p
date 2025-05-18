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
import java.time.LocalDateTime;
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

        // 1. Create 3 rooms with different sizes
        List<Room> rooms = roomRepository.saveAll(List.of(
                Room.builder().rowCount(5).columnCount(5).build(),
                Room.builder().rowCount(8).columnCount(10).build(),
                Room.builder().rowCount(6).columnCount(7).build()
        ));

        // 2. Generate all seats using flatMap + streams
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
                Movie.builder().title("The Dark Knight").description("Gotham vigilante").image("dark_knight.jpg").build()
        ));

        // 4. Generate 3 screenings per movie using streams
        AtomicInteger screeningIndex = new AtomicInteger();
        List<Screening> screenings = movies.stream()
                .flatMap(movie -> IntStream.range(0, 3)
                        .mapToObj(i -> {
                            int index = screeningIndex.getAndIncrement();
                            Room room = rooms.get(index % rooms.size());
                            LocalDateTime start = LocalDateTime.now()
                                    .plusDays(i + 1)
                                    .withHour(14 + i * 2)
                                    .withMinute(0);

                            return Screening.builder()
                                    .movie(movie)
                                    .room(room)
                                    .startTime(start)
                                    .duration(Duration.ofMinutes(120 + (i * 10L)))
                                    .build();
                        }))
                .toList();

        screeningRepository.saveAll(screenings);

        System.out.println("✅ Database fully initialized with:");
        System.out.println("- " + rooms.size() + " rooms");
        System.out.println("- " + allSeats.size() + " seats");
        System.out.println("- " + movies.size() + " movies");
        System.out.println("- " + screenings.size() + " screenings");
    }

}
