package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.TicketSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface TicketSeatRepository extends JpaRepository<TicketSeat, Long> {

    @Query("SELECT ts.seat.id FROM TicketSeat ts WHERE ts.screening.id = :screeningId")
    Set<Long> findTakenSeatIds(@Param("screeningId") Long screeningId);
}

