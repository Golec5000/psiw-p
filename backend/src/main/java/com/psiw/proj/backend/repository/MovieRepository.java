package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.Movie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * Zwraca wszystkie filmy mające seanse w danym przedziale
     * – ładuje od razu screenings + room zgodnie z grafem.
     */
    @EntityGraph(value = "Movie.withScreeningsAndRoom", type = EntityGraph.EntityGraphType.FETCH)
    List<Movie> findDistinctByScreeningsStartTimeBetween(LocalDateTime from, LocalDateTime to);

}
