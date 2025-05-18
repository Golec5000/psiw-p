package com.psiw.proj.backend.exeptions.custom;

public class RoomHasNoSeatsException extends RuntimeException {
    public RoomHasNoSeatsException(String message) {
        super(message);
    }
}
