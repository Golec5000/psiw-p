package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.service.interfaces.AuthService;
import com.psiw.proj.backend.utils.aspects.LogExecution;
import com.psiw.proj.backend.utils.responseDto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@LogExecution
@RestController
@RequiredArgsConstructor
@RequestMapping("/psiw/api/v1/auth")
public class LoginController {

    private final AuthService authService;

    @Operation(
            summary = "Logowanie użytkownika",
            description = "Umożliwia zalogowanie użytkownika na podstawie danych uwierzytelniających przekazanych przez Spring Security. Zwraca token JWT po poprawnej autoryzacji."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zalogowano pomyślnie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany - błędne dane logowania", content = @Content),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(Authentication authentication) {
        return ResponseEntity.ok(authService.login(authentication));
    }
}
