package com.psiw.proj.backend.service.interfaces;

import com.psiw.proj.backend.utils.TicketStatus;
import com.psiw.proj.backend.utils.responseDto.TicketResponse;

import java.util.UUID;

public interface TicketValidationService {

    TicketStatus checkTicket(UUID ticketNumber);

    TicketResponse scanTicket(UUID ticketNumber);

}
