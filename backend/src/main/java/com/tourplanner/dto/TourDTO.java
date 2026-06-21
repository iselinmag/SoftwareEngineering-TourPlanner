package com.tourplanner.dto;

import com.tourplanner.entity.Tour.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TourDTO {
    private Long id;
    private String ownerUsername;   // who created this tour, used by the frontend to show or hide edit and delete

    // name must be filled in, no blanks
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "From location is required")
    private String fromLocation;

    @NotBlank(message = "To location is required")
    private String toLocation;

    // transport type must be chosen
    @NotNull(message = "Transport type is required")
    private TransportType transportType;

    // distance and time are calculated by the backend, so we do not require them here
    private Double distance;
    private String estimatedTime;
    private String routeInformation;

    // Computed attributes (derived, not stored directly)
    private int popularity;            // score 0–100 combining avg rating and log volume
    private String popularityLevel;    // human-readable label for the score
    private String childFriendliness;  // derived from avg log difficulty
}
