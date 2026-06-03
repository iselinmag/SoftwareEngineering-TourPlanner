package com.tourplanner.dto;

import com.tourplanner.entity.Tour.TransportType;
import lombok.Data;

@Data
public class TourDTO {
    private Long id;
    private String name;
    private String description;
    private String fromLocation;
    private String toLocation;
    private TransportType transportType;
    private Double distance;
    private String estimatedTime;
    private String routeInformation;
    // Computed attributes (derived, not stored directly)
    private int popularity;            // score 0–100 combining avg rating and log volume
    private String popularityLevel;    // human-readable label for the score
    private String childFriendliness;  // derived from avg log difficulty
}