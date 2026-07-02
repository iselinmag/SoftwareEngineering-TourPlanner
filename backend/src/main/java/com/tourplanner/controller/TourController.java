package com.tourplanner.controller;

import com.tourplanner.dto.TourDTO;
import com.tourplanner.service.TourService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

// this is the front door for everything to do with tours.
// a controller just listens for browser requests and passes the work to the tour service.
// every address here starts with /api/tours, and only our frontend is allowed to call it.
@RestController
@RequestMapping("/api/tours")
@CrossOrigin(origins = "http://localhost:4200")
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    // GET /api/tours gives back the full list of tours
    @GetMapping
    public List<TourDTO> getAllTours() {
        return tourService.getAllTours();
    }

    // GET /api/tours/1 gives back the one tour with id 1
    @GetMapping("/{id}")
    public ResponseEntity<TourDTO> getTourById(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.getTourById(id));
    }

    // GET /api/tours/search?query=vienna gives back the tours that match the typed word
    @GetMapping("/search")
    public List<TourDTO> searchTours(@RequestParam String query) {
        return tourService.searchTours(query);
    }

    // POST /api/tours takes a new tour sent in the request and saves it, then hands it back
    @PostMapping
    public ResponseEntity<TourDTO> createTour(@Valid @RequestBody TourDTO dto) {
        TourDTO created = tourService.createTour(dto);
        return ResponseEntity.status(201).body(created);
    }

    // PUT /api/tours/1 replaces the details of tour 1 with the ones sent in the request
    @PutMapping("/{id}")
    public ResponseEntity<TourDTO> updateTour(@PathVariable Long id, @Valid @RequestBody TourDTO dto) {
        return ResponseEntity.ok(tourService.updateTour(id, dto));
    }

    // DELETE /api/tours/1 removes tour 1, then replies with an empty "all done" message
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/tours/export hands back every tour with its logs, so you can save them to a file
    @GetMapping("/export")
    public ResponseEntity<List<TourDTO>> exportTours() {
        return ResponseEntity.ok(tourService.exportTours());
    }

    // POST /api/tours/import takes a file of saved tours and adds them all back in
    @PostMapping("/import")
    public ResponseEntity<Void> importTours(@RequestBody List<TourDTO> tours) {
        tourService.importTours(tours);
        return ResponseEntity.status(201).build();
    }
}
