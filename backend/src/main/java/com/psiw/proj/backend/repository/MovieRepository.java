package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.Movie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * Zwraca wszystkie filmy mające seanse w danym przedziale
     * – ładuje od razu screenings + room zgodnie z grafem.
     */
    @EntityGraph(value = "Movie.withScreeningsAndRoom", type = EntityGraph.EntityGraphType.FETCH)
    @Query("""
              SELECT DISTINCT m
              FROM Movie m
              JOIN FETCH m.screenings s
              JOIN FETCH s.room r
              WHERE s.startTime >= :from
                AND s.startTime <  :to
              ORDER BY m.title, s.startTime
            """)
    List<Movie> findDistinctByScreeningsStartTimeBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

}
