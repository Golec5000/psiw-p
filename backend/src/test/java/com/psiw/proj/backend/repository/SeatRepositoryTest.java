package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.Room;
import com.psiw.proj.backend.entity.Seat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
class SeatRepositoryTest {

    @Autowired
    private SeatRepository seatRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldReturnCorrectCountWhenAllSeatsExistInRoom() {
        // given
        Room room = Room.builder()
                .rowCount(10)
                .columnCount(10)
                .build();
        entityManager.persist(room);

        Seat seat1 = createSeat(room, 1);
        Seat seat2 = createSeat(room, 2);
        entityManager.persist(seat1);
        entityManager.persist(seat2);

        entityManager.flush();
        entityManager.clear();

        List<Long> seatIds = List.of(seat1.getId(), seat2.getId());

        // when
        long count = seatRepository.countByIdInAndRoomRoomNumber(seatIds, room.getRoomNumber());

        // then
        assertThat(count).isEqualTo(seatIds.size());
    }

    @Test
    void shouldReturnLowerCountWhenSomeSeatsDoNotExist() {
        // given
        Room room = Room.builder()
                .rowCount(5)
                .columnCount(5)
                .build();
        entityManager.persist(room);

        Seat seat1 = createSeat(room, 1);
        entityManager.persist(seat1);

        entityManager.flush();
        entityManager.clear();

        List<Long> seatIds = List.of(seat1.getId(), 999L); // 999L nie istnieje

        // when
        long count = seatRepository.countByIdInAndRoomRoomNumber(seatIds, room.getRoomNumber());

        // then
        assertThat(count).isLessThan(seatIds.size());
    }

    @Test
    void shouldReturnLowerCountWhenSeatsBelongToDifferentRooms() {
        // given
        Room room1 = Room.builder().rowCount(3).columnCount(3).build();
        Room room2 = Room.builder().rowCount(3).columnCount(3).build();
        entityManager.persist(room1);
        entityManager.persist(room2);

        Seat seat1 = createSeat(room1, 1);
        Seat seat2 = createSeat(room2, 1); // inny pokój
        entityManager.persist(seat1);
        entityManager.persist(seat2);

        entityManager.flush();
        entityManager.clear();

        List<Long> seatIds = List.of(seat1.getId(), seat2.getId());

        // when
        long count = seatRepository.countByIdInAndRoomRoomNumber(seatIds, room1.getRoomNumber());

        // then
        assertThat(count).isLessThan(seatIds.size());
    }

    @Test
    void shouldReturnZeroWhenSeatIdsAreEmpty() {
        // when
        long count = seatRepository.countByIdInAndRoomRoomNumber(Collections.emptyList(), 1L);

        // then
        assertThat(count).isZero();
    }

    private Seat createSeat(Room room, int col) {
        return Seat.builder()
                .room(room)
                .rowNumber(1)
                .columnNumber(col)
                .seatNumber(col)
                .seatPrice(BigDecimal.valueOf(1.0))
                .build();
    }
}
