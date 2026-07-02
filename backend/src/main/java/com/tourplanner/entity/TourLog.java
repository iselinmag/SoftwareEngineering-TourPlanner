package com.tourplanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

// this is the blueprint for one tour log, a single entry someone writes after doing a tour.
// like one page in a diary, it holds the date, a comment, how hard it was, a rating and so on.
// each field is a column and each log object becomes one row in the tour_logs table.
@Entity
@Table(name = "tour_logs")
@Data
@NoArgsConstructor
public class TourLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // many logs belong to one tour, like many diary pages belonging to one trip.
    // the tour_id column is the link back to the tour this log was written for.
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

    // the file name of the image attached to this log, or null if none.
    // the real image lives on disk, this is just the paper slip pointing to it.
    private String imagePath;

    // the short fixed menu of how hard a tour felt (enum, a set list of choices)
    public enum Difficulty {
        Easy, Medium, Hard
    }

    // the user who wrote this log, the user_id column is the name tag saying who owns it
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}
