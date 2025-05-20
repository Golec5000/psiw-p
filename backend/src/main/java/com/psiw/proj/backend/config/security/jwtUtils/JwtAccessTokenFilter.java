package com.psiw.proj.backend.config.security.jwtUtils;

import com.psiw.proj.backend.config.security.RSAKeyRecord;
import com.psiw.proj.backend.utils.enums.TokenType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAccessTokenFilter extends OncePerRequestFilter {

    private final RSAKeyRecord rsaKeyRecord;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(TokenType.Bearer.name())) {
                filterChain.doFilter(request, response);
                return;
            }

            final String token = authHeader.substring(7);

            JwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(rsaKeyRecord.publicKey()).build();
            final Jwt jwtToken = jwtDecoder.decode(token);

            final String userName = jwtTokenUtils.getUserName(jwtToken);

            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = jwtTokenUtils.userDetails(userName);

                if (jwtTokenUtils.isTokenValid(jwtToken, userDetails)) {
                    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                    UsernamePasswordAuthenticationToken createdToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    createdToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    securityContext.setAuthentication(createdToken);
                    SecurityContextHolder.setContext(securityContext);
                }
            }
            filterChain.doFilter(request, response);

        } catch (JwtValidationException jwtValidationException) {
            if (jwtValidationException.getMessage().contains("Jwt expired")) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Token has expired");
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, jwtValidationException.getMessage());
            }
        }
    }
}
