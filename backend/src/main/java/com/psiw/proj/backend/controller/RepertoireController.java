package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.service.interfaces.RepertoireService;
import com.psiw.proj.backend.utils.aspects.LogExecution;
import com.psiw.proj.backend.utils.responseDto.MovieResponse;
import com.psiw.proj.backend.utils.responseDto.ScreeningDetailsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "Pobiera filmy z repertuaru na dany dzień",
            description = "Zwraca listę filmów wraz z zaplanowanymi seansami dla konkretnej daty."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista filmów pobrana pomyślnie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovieResponse.class)))
    })
    @GetMapping("/movies")
    public ResponseEntity<List<MovieResponse>> getMoviesForSpecificDate(@RequestParam LocalDate date) {
        return ResponseEntity.ok(repertoireService.getMoviesWithScreeningsForDate(date));
    }

    @Operation(
            summary = "Pobiera szczegóły konkretnego seansu",
            description = "Zwraca szczegółowe informacje na temat seansu na podstawie jego ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Szczegóły seansu zwrócone pomyślnie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScreeningDetailsResponse.class)))
    })
    @GetMapping("/movie-screening")
    public ResponseEntity<ScreeningDetailsResponse> getScreeningDetails(@RequestParam Long screeningId) {
        return ResponseEntity.ok(repertoireService.getScreeningDetails(screeningId));
    }

}
