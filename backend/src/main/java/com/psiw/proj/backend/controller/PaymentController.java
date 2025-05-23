package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.utils.aspects.LogExecution;
import com.psiw.proj.backend.utils.requestDto.PaymentRequest;
import com.psiw.proj.backend.utils.responseDto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@LogExecution
@RestController
@RequestMapping("/psiw/api/v1/open/payment")
public class PaymentController {

    private final AtomicInteger invocationCounter = new AtomicInteger(0);
    private final Lock lock = new ReentrantLock();
    private final int failEveryN = 5;


    @Operation(
            summary = "Przetwarza płatność",
            description = "Endpoint do symulacji przetwarzania płatności. Co piąte żądanie kończy się błędem 503 (symulacja awarii)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Płatność przetworzona pomyślnie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "503", description = "Symulowana awaria przetwarzania płatności",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class)))
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        lock.lock();
        try {
            int attempt = invocationCounter.incrementAndGet();
            if (attempt % failEveryN == 0) {
                invocationCounter.set(0);
                return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new PaymentResponse(false, "Payment failed."));
            }

            return ResponseEntity.ok(new PaymentResponse(true, "Payment succeeded."));
        } finally {
            lock.unlock();
        }
    }

}
