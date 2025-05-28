package com.psiw.proj.backend.utils.requestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Dane wymagane do przetworzenia płatności")
public record PaymentRequest(
        @Schema(description = "Identyfikator użytkownika", example = "12345", required = true)
        @NotNull Long userId,

        @Schema(description = "Kwota płatności", example = "150.75", required = true)
        @NotNull BigDecimal amount,

        @Schema(description = "Metoda płatności", example = "CARD", required = true)
        @NotNull String method
) {}