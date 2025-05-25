package com.psiw.proj.backend.utils.responseDto;

import com.psiw.proj.backend.utils.enums.TokenType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Odpowiedź po logowaniu: access i refresh tokeny")
public record LoginResponse(

        @Schema(description = "JWT dostępowy", example = "eyJhbGciOiJIUzI1NiI...")
        String accessToken,

        @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiI...")
        String refreshToken,

        @Schema(description = "Typ tokenu dostępowego", example = "Bearer")
        TokenType tokenType
) {
}
