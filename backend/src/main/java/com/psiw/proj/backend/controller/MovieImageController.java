package com.psiw.proj.backend.controller;

import com.psiw.proj.backend.service.interfaces.MovieImageService;
import com.psiw.proj.backend.utils.aspects.LogExecution;
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

    @GetMapping("/{id}/image")
    public ResponseEntity<ByteArrayResource> getMovieImage(@PathVariable("id") Long movieId) throws IOException {
        Resource img = imageService.loadImageResource(movieId);

        // Odczyt bajtów
        byte[] data = StreamUtils.copyToByteArray(img.getInputStream());
        ByteArrayResource resource = new ByteArrayResource(data);

        // Rozpoznaj content-type (jpg/png/etc)
        String contentType = Files.probeContentType(
                Paths.get(img.getURI())
        );
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
