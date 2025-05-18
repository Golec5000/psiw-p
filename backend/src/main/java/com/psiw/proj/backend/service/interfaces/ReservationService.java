package com.psiw.proj.backend.service.interfaces;

import com.psiw.proj.backend.utils.responseDto.TicketResponse;

import java.util.List;

public interface ReservationService {

    TicketResponse reserveSeats(Long screeningId, List<Long> seatIds);

}
