package com.psiw.proj.backend.utils.requestDto;

import java.math.BigDecimal;

public record PaymentRequest(
        String orderId,
        BigDecimal amount
) {}