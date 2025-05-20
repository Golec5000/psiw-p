package com.psiw.proj.backend.exeptions.custom;

public class TicketClerkNotFoundException extends RuntimeException {
    public TicketClerkNotFoundException(String message) {
        super(message);
    }
}
