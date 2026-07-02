package com.tourplanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

// this is the blueprint for one tour.
// think of it as the shape of one row in the tours table, each field is a column
// and each tour object we make becomes one row saved in the database.
@Entity                          // tells the database to build a table out of this class
@Table(name = "tours")           // the table is called "tours"
@Data                            // lombok writes the boring getters and setters for us
@NoArgsConstructor               // lombok also adds the empty constructor the database needs
public class Tour {

    @Id                          // the unique number that names this one tour
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // the database hands out the next number by itself
    private Long id;

    @Column(nullable = false)    // a tour must have a name, this can never be left empty
    private String name;

    private String description;
    private String fromLocation;
    private String toLocation;

    // save the travel type as its plain word like "Bike", not a number, so it stays readable
    @Enumerated(EnumType.STRING)
    private TransportType transportType;

    private Double distance;
    private String estimatedTime;

    // the route can be a long piece of text, so we tell the database to give it plenty of room
    @Column(columnDefinition = "TEXT")
    private String routeInformation; // holds the points that draw the route line on the map

    // one tour can hold many logs, like one folder holding many notes.
    // cascade means if the tour is deleted, all its logs are thrown out with it.
    // mappedBy points at the "tour" field inside TourLog so both sides know they belong together.
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TourLog> tourLogs;

    // the short fixed menu of travel types a tour is allowed to pick from (enum, a set list of choices)
    public enum TransportType {
        Walk, Hike, Bike, Car, Boat, Run
    }

    // many tours can belong to one user, like many books signed out on one library card.
    // the user_id column is the name tag that says who owns this tour.
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}