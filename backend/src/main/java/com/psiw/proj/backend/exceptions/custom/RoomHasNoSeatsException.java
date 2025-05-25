package com.psiw.proj.backend.exceptions.custom;

public class RoomHasNoSeatsException extends RuntimeException {
    public RoomHasNoSeatsException(String message) {
        super(message);
    }
}
