package com.psiw.proj.backend.service.interfaces;

import com.psiw.proj.backend.utils.responseDto.MovieResponse;
import com.psiw.proj.backend.utils.responseDto.ScreeningDetailsResponse;

import java.time.LocalDate;
import java.util.List;

public interface RepertoireService {

    List<MovieResponse> getMoviesWithScreeningsForDate(LocalDate date);

    ScreeningDetailsResponse getScreeningDetails(Long screeningId);
}
