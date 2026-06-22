package com.tourplanner.dto;

import com.tourplanner.entity.Tour.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TourDTO {

    private Long id;
    private String ownerUsername;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "From location is required")
    private String fromLocation;

    @NotBlank(message = "To location is required")
    private String toLocation;

    @NotNull(message = "Transport type is required")
    private TransportType transportType;

    private Double distance;
    private String estimatedTime;
    private String routeInformation;

    private int popularity;
    private String popularityLevel;
    private String childFriendliness;

    // Used only for import/export.
    // Normal tour list/details can leave this as null.
    private List<TourLogDTO> tourLogs;
}