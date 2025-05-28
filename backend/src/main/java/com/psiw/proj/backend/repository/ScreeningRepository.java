package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.Screening;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    @EntityGraph(attributePaths = {"movie", "room", "room.seats"})
    @Query("SELECT s FROM Screening s WHERE s.id = :id")
    Optional<Screening> findByIdWithRoomAndMovie(@Param("id") Long id);
}
