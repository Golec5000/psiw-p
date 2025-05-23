package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.service.interfaces.TicketValidationService;
import com.psiw.proj.backend.utils.aspects.LogExecution;
import com.psiw.proj.backend.utils.enums.TicketStatus;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@LogExecution
@RestController
@RequestMapping("/psiw/api/v1/auth/ticket-validation")
@RequiredArgsConstructor
public class TicketValidationController {

    private final TicketValidationService ticketValidationService;

    @Operation(
            summary = "Sprawdza status biletu",
            description = "Zwraca status biletu (VALID, USED, EXPIRED, INVALID) na podstawie jego ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status biletu",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketStatus.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy identyfikator biletu", content = @Content)
    })
    @GetMapping("/check-staus")
    public ResponseEntity<TicketStatus> checkStaus(
            @Parameter(description = "ID biletu do sprawdzenia", example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam UUID ticketId) {
        return ResponseEntity.ok(ticketValidationService.checkTicket(ticketId));
    }

    @Operation(
            summary = "Skanuje bilet",
            description = "Skanuje bilet podczas wejścia, oznaczając go jako użyty i zwracając szczegóły biletu"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bilet zeskanowany pomyślnie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowy identyfikator biletu", content = @Content)
    })
    @PutMapping("/scan")
    public ResponseEntity<TicketResponse> scanTicket(
            @Parameter(description = "ID biletu do zeskanowania", example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam UUID ticketId) {
        return ResponseEntity.ok(ticketValidationService.scanTicket(ticketId));
    }

}
