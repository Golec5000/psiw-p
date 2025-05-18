package com.psiw.proj.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.psiw.proj.backend.utils.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "ticket",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"screening_id", "seat_id"})
        })
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID ticketNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.DONT_EXIST;

    @ManyToOne
    @JoinColumn(name = "screening_id", nullable = false)
    @JsonBackReference("screening-tickets")
    private Screening screening;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    @JsonManagedReference("seat-tickets")
    private Seat seat;
}
