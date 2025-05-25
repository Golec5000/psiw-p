package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.Screening;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    @EntityGraph(attributePaths = {"movie", "room", "room.seats"})
    @Query("SELECT s FROM Screening s WHERE s.id = :id")
    Optional<Screening> findByIdWithRoomAndMovie(@Param("id") Long id);

    @Query("SELECT s FROM Screening s WHERE s.startTime <= :now")
    List<Screening> findStartedScreenings(@Param("now") LocalDateTime now);

}
