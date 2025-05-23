package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.TicketClerk;
import com.psiw.proj.backend.exeptions.custom.TicketClerkNotFoundException;
import com.psiw.proj.backend.repository.TicketClerkRepository;
import com.psiw.proj.backend.service.interfaces.AuthService;
import com.psiw.proj.backend.utils.enums.TokenType;
import com.psiw.proj.backend.utils.responseDto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TicketClerkRepository ticketClerkRepository;
    private final JwtEncoder jwtEncoder;

    @Override
    public LoginResponse login(Authentication authentication) throws TicketClerkNotFoundException {
        TicketClerk user = ticketClerkRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new TicketClerkNotFoundException("User not found"));

        String accessToken = createAccessToken(user);

        return new LoginResponse(accessToken, TokenType.Bearer);
    }

    private String createAccessToken(TicketClerk user) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("harmony-home-net")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
