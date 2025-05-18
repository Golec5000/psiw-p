package com.psiw.proj.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "ticket_seat",
        uniqueConstraints = @UniqueConstraint(
                name = "uc_ticketseat_screening_seat",
                columnNames = {"screening_id", "seat_id"}
        )
)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * wiele TicketSeat → jeden Ticket
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_number", nullable = false)
    @JsonBackReference("ticket-ticketSeats")
    private Ticket ticket;

    /**
     * wiele TicketSeat → jedno Seat
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    @JsonBackReference("seat-ticketSeats")
    private Seat seat;

    /**
     * duplikujemy screening_id, by wymusić UNIQUE(screening_id, seat_id)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    @JsonBackReference("screening-ticketSeats")
    private Screening screening;
}
