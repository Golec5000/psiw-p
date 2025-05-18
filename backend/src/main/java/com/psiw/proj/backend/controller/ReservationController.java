package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.service.interfaces.ReservationService;
import com.psiw.proj.backend.utils.requestDto.ReservationRequest;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/psiw/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/confirm")
    public ResponseEntity<TicketResponse> confirmReservation(@RequestBody @Valid ReservationRequest reservationRequest) {
        return ResponseEntity.ok(reservationService.reserveSeats(reservationRequest.screeningId(), reservationRequest.seatIds()));
    }

}
