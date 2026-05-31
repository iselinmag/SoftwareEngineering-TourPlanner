package com.tourplanner.service;

import com.tourplanner.dto.TourDTO;
import com.tourplanner.entity.Tour;
import com.tourplanner.repository.TourRepository;
import com.tourplanner.repository.TourLogRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service  // Marks this as a Spring service — Spring creates one shared instance
public class TourService {

    // Log4j2 logger — logs messages to console/file
    private static final Logger logger = LogManager.getLogger(TourService.class);

    // Spring automatically injects these (called "constructor injection")
    private final TourRepository tourRepository;
    private final TourLogRepository tourLogRepository;
    private final RouteService routeService;

    public TourService(TourRepository tourRepository,
                   TourLogRepository tourLogRepository,
                   RouteService routeService) {
        this.tourRepository = tourRepository;
        this.tourLogRepository = tourLogRepository;
        this.routeService = routeService;
    }

    // GET all tours
    public List<TourDTO> getAllTours() {
        logger.info("Fetching all tours");
        return tourRepository.findAll()
                .stream()
                .map(this::toDTO)   // convert each Tour entity → TourDTO
                .collect(Collectors.toList());
    }

    // GET single tour by ID
    public TourDTO getTourById(Long id) {
        logger.info("Fetching tour with id {}", id);
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found: " + id));
        return toDTO(tour);
    }

    // CREATE tour
    public TourDTO createTour(TourDTO dto) {
        logger.info("Creating tour: {}", dto.getName());
        Tour tour = toEntity(dto);

        try {
            // call openrouteservice to get real distance and time
            // if it fails we fall back to whatever the user typed in
            RouteService.RouteResult route = routeService.getRoute(
                    dto.getFromLocation(),
                    dto.getToLocation(),
                    dto.getTransportType().name()
            );

            if (route != null) {
                tour.setDistance(route.distanceKm);
                tour.setEstimatedTime(route.estimatedTime);
                tour.setRouteInformation(route.geometryJson); // save the route coordinates
                logger.info("Route data applied: {} km, {}", route.distanceKm, route.estimatedTime);
            } else {
                logger.warn("Route fetch failed, using user-provided values");
            }
        } catch (Exception e) {
            // Route calculation failed — log it but ALWAYS save the tour
            logger.error("Route service threw an exception, saving tour with user values: {}", e.getMessage());
        }

        return toDTO(tourRepository.save(tour));
    }


    // UPDATE tour
    public TourDTO updateTour(Long id, TourDTO dto) {
        logger.info("Updating tour id {}", id);
        Tour existing = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found: " + id));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setFromLocation(dto.getFromLocation());
        existing.setToLocation(dto.getToLocation());
        existing.setTransportType(dto.getTransportType());
        existing.setRouteInformation(dto.getRouteInformation());

        try {
            // recalculate route whenever from/to locations are updated
            RouteService.RouteResult route = routeService.getRoute(
                    dto.getFromLocation(),
                    dto.getToLocation(),
                    dto.getTransportType().name()
            );

            if (route != null) {
                existing.setDistance(route.distanceKm);
                existing.setEstimatedTime(route.estimatedTime);
                existing.setRouteInformation(route.geometryJson);
            } else {
                // keep existing values if the route call fails
                existing.setDistance(dto.getDistance());
                existing.setEstimatedTime(dto.getEstimatedTime());
            }
        } catch (Exception e) {
            // Route calculation failed — log it but ALWAYS save the tour
            logger.error("Route service threw an exception, saving tour with user values: {}", e.getMessage());
        }

        return toDTO(tourRepository.save(existing));
    }

    // DELETE tour
    public void deleteTour(Long id) {
        logger.info("Deleting tour id {}", id);
        tourRepository.deleteById(id);
    }

    // SEARCH tours (full-text)
    public List<TourDTO> searchTours(String query) {
        logger.info("Searching tours with query: {}", query);
        return tourRepository.searchTours(query)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- Helper: convert Entity → DTO ---
    private TourDTO toDTO(Tour tour) {
        TourDTO dto = new TourDTO();
        dto.setId(tour.getId());
        dto.setName(tour.getName());
        dto.setDescription(tour.getDescription());
        dto.setFromLocation(tour.getFromLocation());
        dto.setToLocation(tour.getToLocation());
        dto.setTransportType(tour.getTransportType());
        dto.setDistance(tour.getDistance());
        dto.setEstimatedTime(tour.getEstimatedTime());
        dto.setRouteInformation(tour.getRouteInformation());
        // Computed: popularity = number of logs for this tour
        dto.setPopularity((int) tourLogRepository.countByTourId(tour.getId()));
        // Computed: child-friendliness (simple rule for now)
        dto.setChildFriendliness(computeChildFriendliness(tour));
        return dto;
    }

    // --- Helper: convert DTO → Entity ---
    private Tour toEntity(TourDTO dto) {
        Tour tour = new Tour();
        tour.setName(dto.getName());
        tour.setDescription(dto.getDescription());
        tour.setFromLocation(dto.getFromLocation());
        tour.setToLocation(dto.getToLocation());
        tour.setTransportType(dto.getTransportType());
        tour.setDistance(dto.getDistance());
        tour.setEstimatedTime(dto.getEstimatedTime());
        tour.setRouteInformation(dto.getRouteInformation());
        return tour;
    }

    // Derived attribute: child-friendliness
    // Rule: Easy + short distance = "Child Friendly", otherwise "Not Child Friendly"
    private String computeChildFriendliness(Tour tour) {
        if (tour.getDistance() == null) return "Unknown";
        if (tour.getDistance() <= 5) return "Child Friendly";
        if (tour.getDistance() <= 15) return "Moderate";
        return "Not Child Friendly";
    }
}