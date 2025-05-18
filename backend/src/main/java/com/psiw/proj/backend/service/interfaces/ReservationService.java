package com.psiw.proj.backend.service.interfaces;

import com.psiw.proj.backend.entity.Ticket;

import java.util.List;

public interface ReservationService {

    Ticket reserveSeats(Long screeningId, List<Long> seatIds);

}
