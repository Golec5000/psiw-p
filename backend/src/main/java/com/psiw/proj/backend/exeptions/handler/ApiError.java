package com.psiw.proj.backend.exeptions.handler;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Obiekt błędu API zwracany w przypadku wyjątków")
public record ApiError(
        @Schema(description = "Ścieżka żądania, w którym wystąpił błąd", example = "/psiw/api/v1/auth/login")
        String path,

        @Schema(description = "Komunikat błędu", example = "User not found")
        String message,

        @Schema(description = "Kod statusu HTTP", example = "404")
        int status,

        @Schema(description = "Czas wystąpienia błędu", example = "2025-05-23T13:45:30")
        LocalDateTime timestamp
) {
}