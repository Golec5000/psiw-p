package com.psiw.proj.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "seat", indexes = {
        @Index(name = "idx_seat_room_number", columnList = "room_number")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uc_seat_room_number", columnNames = {"room_number", "column_number", "row_number"})
})
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
    private int rowNumber;

    @Column(name = "column_number", nullable = false)
    private int columnNumber;

    @ManyToOne
    @JoinColumn(name = "room_number", nullable = false)
    @JsonBackReference("room-seats")
    private Room room;

    @OneToMany(mappedBy = "seat")
    @JsonBackReference("seat-tickets")
    private List<Ticket> tickets;

}
