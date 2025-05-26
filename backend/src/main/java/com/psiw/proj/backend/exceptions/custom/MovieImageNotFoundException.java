package com.psiw.proj.backend.exceptions.custom;

public class MovieImageNotFoundException extends RuntimeException {
    public MovieImageNotFoundException(String message) {
        super(message);
    }
}
