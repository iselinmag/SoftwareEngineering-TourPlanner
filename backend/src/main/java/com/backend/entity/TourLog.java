package com.tourplanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "tour_logs")
@Data
@NoArgsConstructor
public class TourLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many logs belong to one tour.
    // @JoinColumn = the "tour_id" foreign key column in tour_logs table
    @ManyToOne
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    private LocalDate dateTime;
    private String comment;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private Double totalDistance;
    private String totalTime;
    private Integer rating;

    public enum Difficulty {
        Easy, Medium, Hard
    }
}