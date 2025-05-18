package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.Ticket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    /**
     * Pobiera wszystkie bilet_seat (czyli miejsca pod tymi biletami) dla seansu
     * w jednym zapytaniu dzięki EntityGraph na relacji ticketSeats.seat.
     */
    @EntityGraph(attributePaths = {"ticketSeats", "ticketSeats.seat"})
    List<Ticket> findAllByScreeningId(Long screeningId);
}
