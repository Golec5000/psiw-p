package com.psiw.proj.backend.utils.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status biletu")
public enum TicketStatus {
    @Schema(description = "Bilet jest ważny i można wejść")
    VALID,

    @Schema(description = "Bilet został już użyty")
    USED,

    @Schema(description = "Bilet stracił ważność – po seansie")
    EXPIRED,

    @Schema(description = "Bilet jest w trakcie przetwarzania – np. podczas zakupu")
    TO_BE_CALCULATED
}