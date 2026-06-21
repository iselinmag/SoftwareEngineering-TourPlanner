package com.tourplanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity                          // This class maps to a database table
@Table(name = "tours")           // The table will be called "tours"
@Data                            // Lombok: auto-generates getters, setters, toString
@NoArgsConstructor               // Lombok: auto-generates empty constructor
public class Tour {

    @Id                          // This field is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // DB auto-increments the ID
    private Long id;

    @Column(nullable = false)    // This column cannot be NULL in the DB
    private String name;

    private String description;
    private String fromLocation;
    private String toLocation;

    @Enumerated(EnumType.STRING) // Store the enum as a string ("Bike", "Hike", etc.)
    private TransportType transportType;

    private Double distance;
    private String estimatedTime;
    
    @Column(columnDefinition = "TEXT")
    private String routeInformation; // Will store map image path or route data

    // One tour can have many logs.
    // cascade = if you delete a tour, all its logs are also deleted.
    // mappedBy = "tour" refers to the "tour" field in TourLog.java
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TourLog> tourLogs;

    // Enum must be defined in the same package or imported
    public enum TransportType {
        Walk, Hike, Bike, Car, Boat, Run
    }

    // many tours can belong to one user
    // the user_id column is the name stamp
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}