package com.psiw.proj.backend.utils;

import com.psiw.proj.backend.entity.*;
import com.psiw.proj.backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class DBInit {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TicketClerkRepository ticketClerkRepository;

    public static final BigDecimal DEFAULT_SEAT_PRICE = new BigDecimal("25.00");

    @PostConstruct
    public void init() {
        if (roomRepository.count() > 0) {
            log.info("Database already initialized, skipping...");
            return;
        }

        // 1. Create rooms
        List<Room> rooms = roomRepository.saveAll(List.of(
                Room.builder().roomNumber("A1").rowCount(5).columnCount(5).build(),
                Room.builder().roomNumber("A2").rowCount(8).columnCount(10).build(),
                Room.builder().roomNumber("B1").rowCount(6).columnCount(7).build(),
                Room.builder().roomNumber("B2").rowCount(10).columnCount(12).build(),
                Room.builder().roomNumber("C1").rowCount(4).columnCount(6).build()
        ));
        log.info("Rooms created: {}", rooms.size());

        // 2. Generate seats
        List<Seat> allSeats = rooms.stream()
                .flatMap(room -> IntStream.rangeClosed(1, room.getRowCount())
                        .boxed()
                        .flatMap(row -> IntStream.rangeClosed(1, room.getColumnCount())
                                .mapToObj(col -> Seat.builder()
                                        .rowNumber(row)
                                        .columnNumber(col)
                                        .seatNumber((row - 1) * room.getColumnCount() + col)
                                        .seatPrice(DEFAULT_SEAT_PRICE)
                                        .room(room)
                                        .build())))
                .toList();
        seatRepository.saveAll(allSeats);
        log.info("Seats created: {}", allSeats.size());

        // 3. Create sample movies
        List<Movie> movies = movieRepository.saveAll(List.of(
                Movie.builder()
                        .title("Inception")
                        .description(
                                "Dom Cobb is a skilled extractor who enters the dreams of others to " +
                                        "steal hidden secrets. When offered a chance to have his criminal record " +
                                        "wiped clean, he must perform an almost impossible ‘inception’: " +
                                        "planting an idea into someone’s subconscious. As the boundaries between " +
                                        "dream and reality blur, Cobb’s own memories threaten to " +
                                        "jeopardize the mission—and his very sanity."
                        )
                        .image("inception.jpg")
                        .build(),

                Movie.builder()
                        .title("The Matrix")
                        .description(
                                "Thomas Anderson, a mild-mannered software developer by day and notorious hacker ‘Neo’ " +
                                        "by night, discovers that the world around him is a simulated reality controlled " +
                                        "by sentient machines. Guided by the enigmatic Morpheus and fierce warrior Trinity, " +
                                        "Neo must embrace his destiny as ‘The One’ to lead humanity’s rebellion. " +
                                        "A groundbreaking fusion of philosophy, cyberpunk aesthetics, and revolutionary action sequences."
                        )
                        .image("matrix.jpg")
                        .build(),

                Movie.builder()
                        .title("Interstellar")
                        .description(
                                "With Earth facing ecological collapse, former pilot Cooper joins a daring mission " +
                                        "through a wormhole to find a new home for humanity. Alongside a team of " +
                                        "scientists, he confronts the profound mysteries of time, gravity, and love, " +
                                        "as he races against time to save his children’s future. A visually stunning " +
                                        "odyssey that explores the bonds that transcend space and the limits of human endurance."
                        )
                        .image("interstellar.jpg")
                        .build(),

                Movie.builder()
                        .title("The Dark Knight")
                        .description(
                                "Gotham City teeters on the brink of anarchy as Batman’s crusade against crime escalates" +
                                        " into a battle of wills with the twisted Joker. As chaos spreads and moral" +
                                        " lines blur, Batman must confront his own code of ethics—or risk losing " +
                                        "everything he’s sworn to protect. A dark, gripping masterpiece that redefined" +
                                        " the superhero genre with its depth, intensity, and unforgettable performances."
                        )
                        .image("dark_knight.jpg")
                        .build(),

                Movie.builder()
                        .title("Avatar")
                        .description(
                                "Sent to the lush moon of Pandora as part of a colonial operation, paraplegic Marine " +
                                        "Jake Sully finds himself torn between his orders and the world of the Na’vi, " +
                                        "Pandora’s indigenous people. Through the Avatar Program, he begins to understand " +
                                        "the profound spiritual connection the Na’vi share with their environment—and " +
                                        "falls in love with both their culture and a tribal princess. James Cameron’s " +
                                        "visionary epic melds cutting-edge visual effects with an environmental parable " +
                                        "of love and conflict."
                        )
                        .image("avatar.jpg")
                        .build(),

                Movie.builder()
                        .title("Parasite")
                        .description(
                                "When the impoverished Kim family cons their way into the lives of the wealthy " +
                                        "Parks, they taste the comforts and privileges of a world far removed from their own. " +
                                        "But as alliances shift and secrets surface, a darkly comedic chain of events spirals " +
                                        "into violence and tragedy. Bong Joon-ho’s Oscar®-winning tour de force skewers class " +
                                        "disparity with razor-sharp wit, unpredictable twists, and unforgettable imagery."
                        )
                        .image("parasite.jpg")
                        .build()
        ));
        log.info("Movies created: {}", movies.size());

        // 4. Generate screenings for 8 days × 5 time slots × all movies
        List<Screening> screenings = new ArrayList<>();
        LocalDate startDate = LocalDate.now();
        List<Integer> startHours = List.of(9, 12, 15, 18, 21);
        Random random = new Random();
        AtomicInteger roomIndex = new AtomicInteger();

        for (Movie movie : movies) {
            for (int dayOffset = 0; dayOffset <= 7; dayOffset++) {
                for (Integer hour : startHours) {
                    Room room = rooms.get(roomIndex.getAndIncrement() % rooms.size());
                    LocalDateTime startTime = startDate.plusDays(dayOffset).atTime(hour, 0);
                    Duration duration = Duration.ofMinutes(100 + random.nextInt(41)); // 100–140 min

                    screenings.add(Screening.builder()
                            .movie(movie)
                            .room(room)
                            .startTime(startTime)
                            .duration(duration)
                            .build());

                    log.info("Screening: {} in room {} at {} for {}min",
                            movie.getTitle(), room.getRoomNumber(), startTime, duration.toMinutes());
                }
            }
        }
        screeningRepository.saveAll(screenings);
        log.info("Screenings created: {}", screenings.size());

        // 5. Create admin users
        for (int i = 0; i < 5; i++) {
            String username = "admin" + i;
            String password = bCryptPasswordEncoder.encode("admin" + i);
            TicketClerk clerk = TicketClerk.builder()
                    .username(username)
                    .password(password)
                    .fullName("Admin User " + i)
                    .build();
            ticketClerkRepository.save(clerk);
            log.info("Admin user created: {}", username);
        }

        log.info("✅ Initialization complete: {} rooms, {} seats, {} movies, {} screenings",
                rooms.size(), allSeats.size(), movies.size(), screenings.size());
    }
}
