package com.tourplanner.dto;

import com.tourplanner.entity.TourLog.Difficulty;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TourLogDTO {
    private Long id;
    private String ownerUsername;   // who created this tour, used by the frontend to show or hide edit and delete
    private Long tourId;          // frontend sends the tour ID, not the full tour object
    private LocalDate dateTime;
    private String comment;
    private Difficulty difficulty;
    private Double totalDistance;
    private String totalTime;
    private Integer rating;
}