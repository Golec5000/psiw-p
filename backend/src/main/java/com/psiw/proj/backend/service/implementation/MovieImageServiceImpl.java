package com.psiw.proj.backend.service.implementation;

import com.psiw.proj.backend.entity.Movie;
import com.psiw.proj.backend.exceptions.custom.MovieImageNotFoundException;
import com.psiw.proj.backend.exceptions.custom.MovieNotFoundException;
import com.psiw.proj.backend.repository.MovieRepository;
import com.psiw.proj.backend.service.interfaces.MovieImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieImageServiceImpl implements MovieImageService {
    private final MovieRepository movieRepository;
    private final ResourceLoader resourceLoader;

    @Value("${movie.images.location}")
    private String imagesLocation;

    @Override
    public Resource loadImageResource(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + movieId));

        String filename = movie.getImage(); // np. "inception.jpg"
        String fullPath = imagesLocation + filename;

        Resource resource = resourceLoader.getResource(fullPath);
        if (!resource.exists() || !resource.isReadable()) {
            throw new MovieImageNotFoundException("Image not found: " + filename);
        }
        return resource;
    }
}

