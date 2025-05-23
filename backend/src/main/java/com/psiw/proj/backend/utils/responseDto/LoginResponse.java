package com.psiw.proj.backend.utils.responseDto;

import com.psiw.proj.backend.utils.enums.TokenType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Odpowiedź zwracana po poprawnym logowaniu zawierająca token JWT i typ tokenu")
public record LoginResponse(

        @Schema(description = "Token JWT autoryzujący użytkownika", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Typ tokenu", example = "Bearer")
        TokenType tokenType

) {
}