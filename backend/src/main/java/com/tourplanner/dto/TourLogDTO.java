package com.tourplanner.dto;

import com.tourplanner.entity.TourLog.Difficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TourLogDTO {
    private Long id;
    private String ownerUsername;   // who wrote this log, used by the frontend to show or hide edit and delete
    private Long tourId;            // frontend sends the tour ID, not the full tour object

    // every log needs a date
    @NotNull(message = "Date is required")
    private LocalDate dateTime;

    // comment must not be blank
    @NotBlank(message = "Comment is required")
    private String comment;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    private Double totalDistance;
    private String totalTime;

    // rating has to sit between 1 and 5, this blocks values like 999
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
}
