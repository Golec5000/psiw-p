package com.psiw.proj.backend.service.implementation;

import com.psiw.proj.backend.entity.Movie;
import com.psiw.proj.backend.entity.Screening;
import com.psiw.proj.backend.entity.Seat;
import com.psiw.proj.backend.exceptions.custom.RoomHasNoSeatsException;
import com.psiw.proj.backend.exceptions.custom.ScreeningNotFoundException;
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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RepertoireServiceImpl implements RepertoireService {

    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;
    private final TicketSeatRepository ticketSeatRepository;

    @Override
    public List<MovieResponse> getMoviesWithScreeningsForDate(LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        List<Movie> movies = movieRepository.findDistinctByScreeningsStartTimeBetween(from, to);

        return createMovieResponse(movies);
    }

    @Override
    public ScreeningDetailsResponse getScreeningDetails(Long screeningId) {
        Screening s = screeningRepository.findByIdWithRoomAndMovie(screeningId)
                .orElseThrow(() -> new ScreeningNotFoundException("Screening not found"));

        Set<Long> taken = ticketSeatRepository.findTakenSeatIds(screeningId);
        List<Seat> seatList = Optional.ofNullable(s.getRoom().getSeats())
                .orElseThrow(() -> new RoomHasNoSeatsException("Room has no seats defined"));

        return createScreeningResponse(s, seatList, taken);
    }

    private ScreeningDetailsResponse createScreeningResponse(Screening s, List<Seat> seatList, Set<Long> taken) {
        return new ScreeningDetailsResponse(
                s.getId(),
                new MovieSimpleDto(s.getMovie().getId(), s.getMovie().getTitle()),
                new RoomDto(s.getRoom().getRoomNumber(), s.getRoom().getRowCount(), s.getRoom().getColumnCount()),
                s.getStartTime(),
                s.getDuration().toMinutes(),
                getSeatDtos(seatList, taken)
        );
    }

    private List<MovieResponse> createMovieResponse(List<Movie> movies) {
        return movies.stream()
                .map(m -> new MovieResponse(
                        m.getId(),
                        m.getTitle(),
                        m.getDescription(),
                        m.getScreenings().stream()
                                .map(this::createScreeningDto)
                                .toList()
                ))
                .toList();
    }

    private List<SeatDto> getSeatDtos(List<Seat> seatList, Set<Long> taken) {
        return seatList.stream()
                .map(seat -> new SeatDto(
                        seat.getId(),
                        seat.getRowNumber(),
                        seat.getColumnNumber(),
                        seat.getSeatNumber(),
                        !taken.contains(seat.getId())
                ))
                .toList();
    }

    private ScreeningSummaryDto createScreeningDto(Screening screening) {
        return ScreeningSummaryDto.builder()
                .id(screening.getId())
                .startTime(screening.getStartTime())
                .duration(screening.getDuration().toMinutes())
                .build();
    }
}
