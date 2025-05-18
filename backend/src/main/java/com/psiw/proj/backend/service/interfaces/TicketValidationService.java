package com.psiw.proj.backend.service.interfaces;

import com.psiw.proj.backend.entity.Ticket;
import com.psiw.proj.backend.utils.TicketStatus;

import java.util.UUID;

public interface TicketValidationService {

    TicketStatus checkTicket(UUID ticketNumber);

    Ticket scanTicket(UUID ticketNumber);

}
