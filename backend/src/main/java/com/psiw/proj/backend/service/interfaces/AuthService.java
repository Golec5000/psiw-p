package com.psiw.proj.backend.service.interfaces;

import com.psiw.proj.backend.exceptions.custom.TicketClerkNotFoundException;
import com.psiw.proj.backend.utils.responseDto.LoginResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    LoginResponse login(Authentication authentication) throws TicketClerkNotFoundException;

    LoginResponse refresh(String refreshToken);
}
