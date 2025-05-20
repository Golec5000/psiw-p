package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.utils.requestDto.PaymentRequest;
import com.psiw.proj.backend.utils.responseDto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/psiw/api/v1/open/payment")
@Slf4j
public class PaymentController {

    private final AtomicInteger invocationCounter = new AtomicInteger(0);
    private final Lock lock = new ReentrantLock();
    private final int failEveryN = 5;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        lock.lock();
        try {
            int attempt = invocationCounter.incrementAndGet();
            log.info("Processing payment attempt #{}", attempt);
            log.info("Payment request: {}", request);
            // co n-ta próba kończy się niepowodzeniem
            if (attempt % failEveryN == 0) {
                log.warn("Simulated failure on attempt #{}", attempt);
                invocationCounter.set(0);
                return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new PaymentResponse(false, "Payment failed."));
            }

            // tutaj normalnie wrzuciłbyś swoją logikę płatności
            log.info("Payment succeeded on attempt #{}", attempt);
            return ResponseEntity.ok(new PaymentResponse(true, "Payment succeeded."));
        } finally {
            lock.unlock();
        }
    }

}
