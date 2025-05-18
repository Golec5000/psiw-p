package com.psiw.proj.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "screening")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Screening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "film_duration", nullable = false)
    private Duration duration;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonBackReference("movie-screenings")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "room_number", nullable = false)
    @JsonBackReference("room-screenings")
    private Room room;

    @OneToMany(mappedBy = "screening", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("screening-tickets")
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "screening", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("screening-ticketSeats")
    private List<TicketSeat> ticketSeats = new ArrayList<>();
}
