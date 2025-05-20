package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.TicketClerk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketClerkRepository extends JpaRepository<TicketClerk, Long> {

    Optional<TicketClerk> findByUsername(String username);

}
