package com.tourplanner.controller;

import com.tourplanner.dto.TourLogDTO;
import com.tourplanner.service.TourLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import jakarta.validation.Valid;

// this is the front door for everything to do with tour logs and their images.
// a controller just listens for browser requests and hands the work to the service.
// every address here starts with a tour id, because a log always belongs to one tour.
@RestController
@RequestMapping("/api/tours/{tourId}")  // every route here lives under one tour
@CrossOrigin(origins = "http://localhost:4200")
public class TourLogController {

    private final TourLogService tourLogService;

    public TourLogController(TourLogService tourLogService) {
        this.tourLogService = tourLogService;
    }

    // GET /api/tours/1/logs → all logs for tour 1
    @GetMapping("/logs")
    public List<TourLogDTO> getLogsForTour(@PathVariable Long tourId) {
        return tourLogService.getLogsForTour(tourId);
    }

    // GET /api/tours/1/images → list of image file names for tour 1's gallery.
    // this sits directly under the tour, not under logs, so it cannot be mistaken
    // for a log id in the url.
    @GetMapping("/images")
    public List<String> getImagesForTour(@PathVariable Long tourId) {
        return tourLogService.getImagesForTour(tourId);
    }

    // POST /api/tours/1/logs → create a log for tour 1
    @PostMapping("/logs")
    public ResponseEntity<TourLogDTO> createLog(@PathVariable Long tourId,
                                                 @Valid @RequestBody TourLogDTO dto) {
        dto.setTourId(tourId);  // ensure tourId from URL is used
        return ResponseEntity.status(201).body(tourLogService.createLog(dto));
    }

    // POST /api/tours/1/logs/5/image → attach an image to log 5
    // the file arrives as multipart form data under the name "file"
    @PostMapping("/logs/{logId}/image")
    public ResponseEntity<TourLogDTO> uploadImage(@PathVariable Long tourId,
                                                  @PathVariable Long logId,
                                                  @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(tourLogService.uploadImage(logId, file));
    }

    // PUT /api/tours/1/logs/5 → update log 5
    @PutMapping("/logs/{logId}")
    public ResponseEntity<TourLogDTO> updateLog(@PathVariable Long tourId,
                                                 @PathVariable Long logId,
                                                 @Valid @RequestBody TourLogDTO dto) {
        dto.setTourId(tourId);
        return ResponseEntity.ok(tourLogService.updateLog(logId, dto));
    }

    // DELETE /api/tours/1/logs/5 → delete log 5
    @DeleteMapping("/logs/{logId}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long tourId,
                                           @PathVariable Long logId) {
        tourLogService.deleteLog(logId);
        return ResponseEntity.noContent().build();
    }
}