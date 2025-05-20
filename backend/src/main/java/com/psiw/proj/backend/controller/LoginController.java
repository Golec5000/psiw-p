package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.service.interfaces.AuthService;
import com.psiw.proj.backend.utils.responseDto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/psiw/api/v1/auth")
public class LoginController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(Authentication authentication) {
        return ResponseEntity.ok(authService.login(authentication));
    }
}
