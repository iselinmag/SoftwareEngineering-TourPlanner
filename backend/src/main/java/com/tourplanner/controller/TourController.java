package com.tourplanner.controller;

import com.tourplanner.dto.TourDTO;
import com.tourplanner.service.TourService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController                  // Combines @Controller + @ResponseBody
@RequestMapping("/api/tours")    // All methods in this class start with /api/tours
@CrossOrigin(origins = "http://localhost:4200")  // Allow Angular to call this, but block others
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    // GET /api/tours → returns all tours
    @GetMapping
    public List<TourDTO> getAllTours() {
        return tourService.getAllTours();
    }

    // GET /api/tours/1 → returns tour with id=1
    @GetMapping("/{id}")
    public ResponseEntity<TourDTO> getTourById(@PathVariable Long id) { // Q: What is response Entity
        return ResponseEntity.ok(tourService.getTourById(id));
    }

    // GET /api/tours/search?query=vienna
    @GetMapping("/search")
    public List<TourDTO> searchTours(@RequestParam String query) {
        return tourService.searchTours(query);
    }

    // POST /api/tours  (body = TourDTO JSON) → creates tour, returns created tour
    @PostMapping
    public ResponseEntity<TourDTO> createTour(@RequestBody TourDTO dto) {
        TourDTO created = tourService.createTour(dto);
        return ResponseEntity.status(201).body(created);
    }

    // PUT /api/tours/1  (body = updated TourDTO JSON) → updates tour
    @PutMapping("/{id}")
    public ResponseEntity<TourDTO> updateTour(@PathVariable Long id, @RequestBody TourDTO dto) {
        return ResponseEntity.ok(tourService.updateTour(id, dto));
    }

    // DELETE /api/tours/1 → deletes tour
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();  // HTTP 204 — success, no content
    }

    // Isaline -> add Export and Import features (functions) and a button in Angular UI
    
}