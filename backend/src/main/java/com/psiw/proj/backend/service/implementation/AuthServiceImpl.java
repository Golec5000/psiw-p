package com.psiw.proj.backend.service.implementation;

import com.psiw.proj.backend.entity.TicketClerk;
import com.psiw.proj.backend.exceptions.custom.RefreshTokenExpiredException;
import com.psiw.proj.backend.exceptions.custom.TicketClerkNotFoundException;
import com.psiw.proj.backend.repository.TicketClerkRepository;
import com.psiw.proj.backend.service.interfaces.AuthService;
import com.psiw.proj.backend.utils.enums.TokenType;
import com.psiw.proj.backend.utils.responseDto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TicketClerkRepository ticketClerkRepository;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final Clock clock;

    private static final long ACCESS_TOKEN_LIFESPAN_MIN = 2;
    private static final long REFRESH_TOKEN_LIFESPAN_MIN = 30;

    @Override
    public LoginResponse login(Authentication auth) {
        TicketClerk user = ticketClerkRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new TicketClerkNotFoundException("User not found"));

        String accessToken = createToken(user, ACCESS_TOKEN_LIFESPAN_MIN, TokenType.Bearer);
        String refreshToken = createToken(user, REFRESH_TOKEN_LIFESPAN_MIN, TokenType.Refresh);

        return new LoginResponse(accessToken, refreshToken, TokenType.Bearer);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        // 1) zdejmij podpis i sprawdź poprawność
        Jwt jwt = jwtDecoder.decode(refreshToken);

        // 2) upewnij się, że to rzeczywiście refresh token
        if (!TokenType.Refresh.name().equals(jwt.getClaimAsString("type"))) {
            throw new IllegalArgumentException("Unsupported token type");
        }

        // 3) sprawdź, czy token nie jest przeterminowany
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null || expiresAt.isBefore(clock.instant())) {
            throw new RefreshTokenExpiredException("Refresh token has expired");
        }

        // 4) pobierz usera
        String username = jwt.getSubject();
        TicketClerk user = ticketClerkRepository.findByUsername(username)
                .orElseThrow(() -> new TicketClerkNotFoundException("User not found"));

        // 5) wygeneruj nowe tokeny
        String newAccess = createToken(user, ACCESS_TOKEN_LIFESPAN_MIN, TokenType.Bearer);
        String newRefresh = createToken(user, REFRESH_TOKEN_LIFESPAN_MIN, TokenType.Refresh);

        return new LoginResponse(newAccess, newRefresh, TokenType.Bearer);
    }

    private String createToken(TicketClerk user, long lifespanMin, TokenType type) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("cinema-backend")
                .issuedAt(now)
                .expiresAt(now.plus(lifespanMin, ChronoUnit.MINUTES))
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("type", type.name())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
