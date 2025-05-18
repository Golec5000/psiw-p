package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.service.interfaces.TicketValidationService;
import com.psiw.proj.backend.utils.TicketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/psiw/api/v1/ticket-validation")
@RequiredArgsConstructor
public class TicketValidationController {

    private final TicketValidationService ticketValidationService;

    //TODO: Implement authentication for this controller

    @GetMapping("/check-staus")
    public ResponseEntity<TicketStatus> checkStaus(@RequestParam UUID ticketId) {
        return ResponseEntity.ok(ticketValidationService.checkTicket(ticketId));
    }

    @PutMapping("/scan")
    public ResponseEntity<Ticket> scanTicket(@RequestParam UUID ticketId) {
        return ResponseEntity.ok(ticketValidationService.scanTicket(ticketId));
    }

}
