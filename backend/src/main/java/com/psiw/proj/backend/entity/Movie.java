package com.psiw.proj.backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movie", indexes = {
        @Index(name = "idx_movie_title_unq", columnList = "title", unique = true)
})
@NamedEntityGraph(
        name = "Movie.withScreeningsAndRoom",
        attributeNodes = @NamedAttributeNode(
                value = "screenings",
                subgraph = "screenings-room-subgraph"
        ),
        subgraphs = @NamedSubgraph(
                name = "screenings-room-subgraph",
                attributeNodes = @NamedAttributeNode("room")
        )
)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String image;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("movie-screenings")
    private List<Screening> screenings = new ArrayList<>();
}
