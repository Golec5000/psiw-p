package com.psiw.proj.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.psiw.proj.backend.utils.enums.TicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ticket", indexes = {
        @Index(name = "idx_ticket_ticket_number_unq", columnList = "ticket_number", unique = true)
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @Column(name = "ticket_number")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID ticketNumber;

    @Column(name = "ticket_price", nullable = false)
    private BigDecimal ticketPrice;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "owner_surname", nullable = false)
    private String ownerSurname;

    @Email
    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.INVALID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("ticket-screening")
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    /**
     * Jeden Ticket → wiele TicketSeat
     */
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("ticket-ticketSeats")
    private List<TicketSeat> ticketSeats = new ArrayList<>();
}
