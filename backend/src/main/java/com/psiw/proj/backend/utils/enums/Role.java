package com.psiw.proj.backend.utils.enums;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
public enum Role implements GrantedAuthority {
    ROLE_EMPLOYEE;

    @Override
    public String getAuthority() {
        return name();
    }
}
