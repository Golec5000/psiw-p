package com.psiw.proj.backend.utils.requestDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dane wymagane do przetworzenia płatności")
public record PaymentRequest(
        @Schema(description = "Identyfikator użytkownika", example = "12345")
        Long userId,

        @Schema(description = "Kwota płatności", example = "150.75")
        Double amount,

        @Schema(description = "Metoda płatności", example = "CARD")
        String method
) {}