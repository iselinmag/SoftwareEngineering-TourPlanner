package com.tourplanner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

// this is the blueprint for one tour.
// think of it as the shape of one row in the tours table, each field is a column
// and each tour object we make becomes one row saved in the database.
@Entity                          // tells the database to build a table out of this class
@Table(name = "tours")           // the table is called "tours"
@Getter                          // lombok writes the boring getters for us
@Setter                          // and the setters too
@NoArgsConstructor               // lombok also adds the empty constructor the database needs
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
