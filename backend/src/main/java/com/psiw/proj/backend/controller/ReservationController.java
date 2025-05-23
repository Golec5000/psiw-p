package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.service.interfaces.ReservationService;
import com.psiw.proj.backend.utils.aspects.LogExecution;
import com.psiw.proj.backend.utils.requestDto.ReservationRequest;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@LogExecution
@RestController
@RequestMapping("/psiw/api/v1/open/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(
            summary = "Potwierdzenie rezerwacji miejsc",
            description = "Umożliwia użytkownikowi potwierdzenie rezerwacji miejsc na konkretny seans, generując bilet z unikalnym identyfikatorem."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rezerwacja została potwierdzona",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content)
    })
    @PostMapping("/confirm")
    public ResponseEntity<TicketResponse> confirmReservation(@RequestBody @Valid ReservationRequest reservationRequest) {
        return ResponseEntity.ok(reservationService.reserveSeats(reservationRequest));
    }

}
