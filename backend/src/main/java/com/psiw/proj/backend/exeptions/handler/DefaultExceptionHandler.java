package com.psiw.proj.backend.exeptions.handler;

import com.psiw.proj.backend.exeptions.custom.RoomHasNoSeatsException;
import com.psiw.proj.backend.exeptions.custom.ScreeningNotFoundException;
import com.psiw.proj.backend.exeptions.custom.TicketNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler({
            NullPointerException.class, IllegalStateException.class,
            IndexOutOfBoundsException.class, UnsupportedOperationException.class,
            TicketNotFoundException.class, ScreeningNotFoundException.class,
            RoomHasNoSeatsException.class
    })
    public ResponseEntity<ApiError> handleNotFoundExceptions(Exception e, HttpServletRequest request) {
        return createResponseEntity(e, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            IllegalArgumentException.class, RuntimeException.class
    })
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        return createResponseEntity(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        return createResponseEntity(e, request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception e, HttpServletRequest request) {
        return createResponseEntity(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> createResponseEntity(Exception e, HttpServletRequest request, HttpStatus status) {
        ApiError apiError = new ApiError(request.getRequestURI(), e.getMessage(), status.value(), LocalDateTime.now());
        return new ResponseEntity<>(apiError, status);
    }

}
