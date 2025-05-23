package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.service.interfaces.TicketValidationService;
import com.psiw.proj.backend.utils.aspects.LogExecution;
import com.psiw.proj.backend.utils.enums.TicketStatus;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;
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

    @GetMapping("/check-staus")
    public ResponseEntity<TicketStatus> checkStaus(@RequestParam UUID ticketId) {
        return ResponseEntity.ok(ticketValidationService.checkTicket(ticketId));
    }

    @PutMapping("/scan")
    public ResponseEntity<TicketResponse> scanTicket(@RequestParam UUID ticketId) {
        return ResponseEntity.ok(ticketValidationService.scanTicket(ticketId));
    }

}
