package com.psiw.proj.backend.exeptions.custom;

public class ScreeningNotFoundException extends RuntimeException {
    public ScreeningNotFoundException(String message) {
        super(message);
    }
}
