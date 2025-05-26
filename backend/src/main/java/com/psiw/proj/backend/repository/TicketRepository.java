package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.utils.enums.TicketStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    /**
     * Pobiera wszystkie bilet_seat (czyli miejsca pod tymi biletami) dla seansu
     * w jednym zapytaniu dzięki EntityGraph na relacji ticketSeats.seat.
     */
    @EntityGraph(attributePaths = {"ticketSeats", "ticketSeats.seat"})
    List<Ticket> findAllByScreeningId(Long screeningId);

    @Modifying
    @Query("""
                UPDATE Ticket t
                SET t.status = :newStatus
                WHERE t.status = :currentStatus
                  AND EXISTS (
                      SELECT s FROM Screening s
                      WHERE s = t.screening
                        AND s.startTime BETWEEN :now AND :nowPlus15
                  )
            """)
    int updateStatusToValid(
            @Param("newStatus") TicketStatus newStatus,
            @Param("currentStatus") TicketStatus currentStatus,
            @Param("now") LocalDateTime now,
            @Param("nowPlus15") LocalDateTime nowPlus15
    );

    @Modifying
    @Query("""
        UPDATE Ticket t
        SET t.status = 'EXPIRED'
        WHERE t.status IN ('VALID','TO_BE_CALCULATED')
          AND t.screening.id IN :screeningIds
    """)
    int expirePastTicketsByScreening(
            @Param("screeningIds") List<Long> screeningIds
    );
}
