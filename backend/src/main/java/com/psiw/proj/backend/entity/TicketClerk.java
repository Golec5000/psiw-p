package com.psiw.proj.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.psiw.proj.backend.utils.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "ticket_clerks", indexes = {
        @Index(name = "idx_ticketclerk_username_unq", columnList = "username", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketClerk implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.ROLE_EMPLOYEE;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role);
    }

    @Override
    public String getUsername() {
        return username;
    }

}