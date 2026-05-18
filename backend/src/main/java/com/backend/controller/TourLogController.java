package com.tourplanner.controller;

import com.tourplanner.dto.TourLogDTO;
import com.tourplanner.service.TourLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tours/{tourId}/logs")  // Nested under tours
@CrossOrigin(origins = "http://localhost:4200")
public class TourLogController {

    private final TourLogService tourLogService;

    public TourLogController(TourLogService tourLogService) {
        this.tourLogService = tourLogService;
    }

    // GET /api/tours/1/logs → all logs for tour 1
    @GetMapping
    public List<TourLogDTO> getLogsForTour(@PathVariable Long tourId) {
        return tourLogService.getLogsForTour(tourId);
    }

    // POST /api/tours/1/logs → create a log for tour 1
    @PostMapping
    public ResponseEntity<TourLogDTO> createLog(@PathVariable Long tourId,
                                                 @RequestBody TourLogDTO dto) {
        dto.setTourId(tourId);  // ensure tourId from URL is used
        return ResponseEntity.status(201).body(tourLogService.createLog(dto));
    }

    // PUT /api/tours/1/logs/5 → update log 5
    @PutMapping("/{logId}")
    public ResponseEntity<TourLogDTO> updateLog(@PathVariable Long tourId,
                                                 @PathVariable Long logId,
                                                 @RequestBody TourLogDTO dto) {
        dto.setTourId(tourId);
        return ResponseEntity.ok(tourLogService.updateLog(logId, dto));
    }

    // DELETE /api/tours/1/logs/5 → delete log 5
    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long tourId,
                                           @PathVariable Long logId) {
        tourLogService.deleteLog(logId);
        return ResponseEntity.noContent().build();
    }
}