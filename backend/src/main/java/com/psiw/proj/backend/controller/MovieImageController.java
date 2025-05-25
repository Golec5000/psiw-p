package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.service.interfaces.MovieImageService;
import com.psiw.proj.backend.utils.aspects.LogExecution;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@LogExecution
@RequestMapping("/psiw/api/v1/open/movies")
@RequiredArgsConstructor
public class MovieImageController {

    private final MovieImageService imageService;

    @Operation(
            summary = "Pobierz obraz filmu",
            description = "Zwraca surowe bajty obrazu powiązanego z podanym ID filmu. " +
                    "Typ zawartości odpowiedzi jest określany dynamicznie (np. image/jpeg, image/png)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Obraz został pomyślnie pobrany"),
            @ApiResponse(responseCode = "404", description = "Film lub jego obraz nie został znaleziony"),
            @ApiResponse(responseCode = "500", description = "Wewnętrzny błąd serwera podczas odczytu obrazu")
    })
    @GetMapping("/{id}/image")
    public ResponseEntity<ByteArrayResource> getMovieImage(
            @Parameter(
                    description = "Unikalny identyfikator filmu",
                    example = "42",
                    required = true
            )
            @PathVariable("id") Long movieId
    ) throws IOException {
        Resource img = imageService.loadImageResource(movieId);

        // Odczyt bajtów
        byte[] data = StreamUtils.copyToByteArray(img.getInputStream());
        ByteArrayResource resource = new ByteArrayResource(data);

        // Rozpoznaj content-type (jpg/png/etc)
        String contentType = Files.probeContentType(Paths.get(img.getURI()));
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + img.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(data.length)
                .body(resource);
    }
}
