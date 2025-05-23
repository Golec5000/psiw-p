package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.service.interfaces.RepertoireService;
import com.psiw.proj.backend.utils.aspects.LogExecution;
import com.psiw.proj.backend.utils.responseDto.MovieResponse;
import com.psiw.proj.backend.utils.responseDto.ScreeningDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@LogExecution
@RestController
@RequestMapping("/psiw/api/v1/open/repertoire")
@RequiredArgsConstructor
public class RepertoireController {

    private final RepertoireService repertoireService;

    @GetMapping("/movies")
    public ResponseEntity<List<MovieResponse>> getMoviesForSpecificDate(@RequestParam LocalDate date) {
        return ResponseEntity.ok(repertoireService.getMoviesWithScreeningsForDate(date));
    }

    @GetMapping("/movie-screening")
    public ResponseEntity<ScreeningDetailsResponse> getScreeningDetails(@RequestParam Long screeningId) {
        return ResponseEntity.ok(repertoireService.getScreeningDetails(screeningId));
    }

}
