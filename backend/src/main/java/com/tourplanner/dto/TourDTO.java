package com.tourplanner.dto;

import com.tourplanner.entity.Tour.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

// this is the tour shape we actually send to and from the frontend.
// the real Tour entity carries database bits the browser does not need, so this is
// the tidy travel version of it, like packing only what you need into a suitcase.
// it also carries a few extras the frontend likes to show, such as popularity.
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

    // only filled in when we export or import tours together with their logs.
    // the normal tour list and details screens leave this empty.
    private List<TourLogDTO> tourLogs;
}