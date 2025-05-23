package com.psiw.proj.backend.utils.requestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Dane wymagane do rezerwacji miejsc na seansie")
public record ReservationRequest(

        @Schema(description = "ID seansu, na który ma zostać dokonana rezerwacja", example = "123")
        @NotNull
        Long screeningId,

        @Schema(description = "Lista identyfikatorów miejsc do rezerwacji", example = "[1, 2, 3]")
        @NotNull @NotEmpty
        List<Long> seatIds,

        @Schema(description = "Adres email osoby rezerwującej", example = "john.doe@example.com")
        @NotNull @Email
        String email,

        @Schema(description = "Imię osoby rezerwującej", example = "John")
        @NotNull @NotEmpty
        String name,

        @Schema(description = "Nazwisko osoby rezerwującej", example = "Doe")
        @NotNull @NotEmpty
        String surname

) {}