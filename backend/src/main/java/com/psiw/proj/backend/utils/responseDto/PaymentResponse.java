package com.psiw.proj.backend.utils.responseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Odpowiedź po przetworzeniu płatności")
public record PaymentResponse(

        @Schema(description = "Czy płatność się powiodła", example = "true")
        boolean success,

        @Schema(description = "Komunikat informacyjny", example = "Payment succeeded.")
        String message

) {}