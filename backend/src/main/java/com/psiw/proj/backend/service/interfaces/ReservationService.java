package com.psiw.proj.backend.service.interfaces;

import com.psiw.proj.backend.utils.requestDto.ReservationRequest;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;

public interface ReservationService {

    TicketResponse reserveSeats(ReservationRequest reservationRequest);

}
