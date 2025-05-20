package com.psiw.proj.backend.utils.responseDto;

import com.psiw.proj.backend.utils.enums.TokenType;
import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        TokenType tokenType
) {
}