package com.psiw.proj.backend.exceptions.custom;

public class TicketClerkNotFoundException extends RuntimeException {
    public TicketClerkNotFoundException(String message) {
        super(message);
    }
}
