package com.psiw.proj.backend.utils.responseDto;

import lombok.Builder;

@Builder
public record PaymentResponse(
        boolean success,
        String message
) {}
