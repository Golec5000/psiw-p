package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    /**
     * Zlicza, ile spośród seatIds:
     *  – istnieje w tabeli seat
     *  – należy do podanego roomNumber
     * Jeśli wynik == seatIds.size(), to znaczy, że wszystkie istnieją i są w tej samej sali.
     */
    long countByIdInAndRoomRoomNumber(Collection<Long> seatIds, String roomNumber);
}