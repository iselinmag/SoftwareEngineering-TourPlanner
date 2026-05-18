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
    private int popularity;       // number of logs
    private String childFriendliness;  // derived from difficulty/distance/time
}