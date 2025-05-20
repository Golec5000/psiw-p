package com.psiw.proj.backend.utils.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReservationRequest(
        @NotNull
        Long screeningId,

        @NotNull
        @NotEmpty
        List<Long> seatIds,

        @NotNull
        @Email
        String email,

        @NotNull
        @NotEmpty
        String name,

        @NotNull
        @NotEmpty
        String surname
) {
}