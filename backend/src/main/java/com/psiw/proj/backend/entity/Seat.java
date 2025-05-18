package com.psiw.proj.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "seat",
        indexes = @Index(name = "idx_seat_room_number", columnList = "room_number"),
        uniqueConstraints = @UniqueConstraint(
                name = "uc_seat_room_number",
                columnNames = {"room_number", "column_number", "row_number"}
        )
)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_number", nullable = false)
    @Min(value = 1, message = "Row number must be greater than 0")
    private int rowNumber;

    @Column(name = "column_number", nullable = false)
    @Min(value = 1, message = "Column number must be greater than 0")
    private int columnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_number", nullable = false)
    @JsonBackReference("room-seats")
    private Room room;

    /**
     * Jedno Seat → wiele TicketSeat
     */
    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonBackReference("seat-ticketSeats")
    private List<TicketSeat> ticketSeats = new ArrayList<>();
}
