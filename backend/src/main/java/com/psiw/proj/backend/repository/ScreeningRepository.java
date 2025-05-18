package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
}
