package com.psiw.proj.backend.service;

import com.psiw.proj.backend.entity.Movie;
import com.psiw.proj.backend.entity.Screening;
import com.psiw.proj.backend.entity.Seat;
import com.psiw.proj.backend.exeptions.custom.RoomHasNoSeatsException;
import com.psiw.proj.backend.exeptions.custom.ScreeningNotFoundException;
import com.psiw.proj.backend.repository.MovieRepository;
import com.psiw.proj.backend.repository.ScreeningRepository;
import com.psiw.proj.backend.repository.TicketSeatRepository;
import com.psiw.proj.backend.service.interfaces.RepertoireService;
import com.psiw.proj.backend.utils.responseDto.MovieResponse;
import com.psiw.proj.backend.utils.responseDto.ScreeningDetailsResponse;
import com.psiw.proj.backend.utils.responseDto.helpers.MovieSimpleDto;
import com.psiw.proj.backend.utils.responseDto.helpers.RoomDto;
import com.psiw.proj.backend.utils.responseDto.helpers.ScreeningSummaryDto;
import com.psiw.proj.backend.utils.responseDto.helpers.SeatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class RepertoireServiceImpl implements RepertoireService {

    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;
    private final TicketSeatRepository ticketSeatRepository;

    @Override
    public List<MovieResponse> getMoviesWithScreeningsForDate(LocalDate date) {
        log.info("Fetching movies with screenings for date: {}", date);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        List<Movie> movies = movieRepository.findDistinctByScreeningsStartTimeBetween(from, to);
        log.info("Found {} movies with screenings between {} and {}", movies.size(), from, to);

        List<MovieResponse> responses = movies.stream()
                .map(m -> new MovieResponse(
                        m.getId(),
                        m.getTitle(),
                        m.getDescription(),
                        m.getImage(),
                        m.getScreenings().stream()
                                .map(s -> new ScreeningSummaryDto(
                                        s.getId(),
                                        s.getStartTime(),
                                        s.getDuration()
                                ))
                                .toList()
                ))
                .toList();

        log.info("Returning {} movie responses", responses.size());
        return responses;
    }

    @Override
    public ScreeningDetailsResponse getScreeningDetails(Long screeningId) {
        log.info("Fetching screening details for screeningId: {}", screeningId);
        Screening s = screeningRepository.findByIdWithRoomAndMovie(screeningId)
                .orElseThrow(() -> {
                    log.error("Screening not found for id: {}", screeningId);
                    return new ScreeningNotFoundException("Screening not found");
                });

        Set<Long> taken = ticketSeatRepository.findTakenSeatIds(screeningId);
        log.info("Taken seat ids for screening {}: {}", screeningId, taken);

        List<Seat> seatList = Optional.ofNullable(s.getRoom().getSeats())
                .orElseThrow(() -> {
                    log.error("Room has no seats defined for screeningId: {}", screeningId);
                    return new RoomHasNoSeatsException("Room has no seats defined");
                });

        List<SeatDto> seats = seatList.stream()
                .map(seat -> new SeatDto(
                        seat.getId(),
                        seat.getRowNumber(),
                        seat.getColumnNumber(),
                        !taken.contains(seat.getId())
                ))
                .toList();

        log.info("Returning screening details for screeningId: {}", screeningId);
        return new ScreeningDetailsResponse(
                s.getId(),
                new MovieSimpleDto(s.getMovie().getId(), s.getMovie().getTitle()),
                new RoomDto(s.getRoom().getRoomNumber(), s.getRoom().getRowCount(), s.getRoom().getColumnCount()),
                s.getStartTime(),
                s.getDuration(),
                seats
        );
    }
}
