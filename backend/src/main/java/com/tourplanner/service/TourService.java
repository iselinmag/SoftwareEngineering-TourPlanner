package com.tourplanner.service;

import com.tourplanner.dto.TourDTO;
import com.tourplanner.entity.Tour;
import com.tourplanner.exception.ForbiddenException;
import com.tourplanner.exception.NotFoundException;
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
    private final CurrentUser currentUser;

    public TourService(TourRepository tourRepository,
                   TourLogRepository tourLogRepository,
                   RouteService routeService,
                   CurrentUser currentUser) {
        this.tourRepository = tourRepository;
        this.tourLogRepository = tourLogRepository;
        this.routeService = routeService;
        this.currentUser = currentUser;
    }

    // GET all tours, everyone can see every tour in the open model
    public List<TourDTO> getAllTours() {
        logger.info("Fetching all tours");
        return tourRepository.findAll()
                .stream()
                .map(this::toDTO)   // convert each Tour entity → TourDTO
                .collect(Collectors.toList());
    }

    // GET single tour by ID, anyone can view any tour
    public TourDTO getTourById(Long id) {
        logger.info("Fetching tour with id {}", id);
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found: " + id));
        return toDTO(tour);
    }

    // CREATE tour
    public TourDTO createTour(TourDTO dto) {
        logger.info("Creating tour: {}", dto.getName());
        Tour tour = toEntity(dto);
        tour.setUser(currentUser.get());   // stamp the owner before saving

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


    // UPDATE tour, only the owner of the tour may edit it
    public TourDTO updateTour(Long id, TourDTO dto) {
        logger.info("Updating tour id {}", id);
        Tour existing = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found: " + id));

        if (!existing.getUser().getId().equals(currentUser.get().getId())) {
            throw new ForbiddenException("This tour is not yours");
        }

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

    // DELETE tour, only the owner of the tour may delete it
    public void deleteTour(Long id) {
        logger.info("Deleting tour id {}", id);

        // load the tour first so we can check who owns it
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found: " + id));

        // the guard reading the name tag
        if (!tour.getUser().getId().equals(currentUser.get().getId())) {
            throw new ForbiddenException("This tour is not yours");
        }

        tourRepository.deleteById(id);
    }

    // SEARCH tours (full-text) across all tours
    public List<TourDTO> searchTours(String query) {
        logger.info("Searching tours with query: {}", query);
        return tourRepository.searchTours(query)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Teamate task: Needs to be added: Also export the tour logs for each tour not only tours.
    // EXPORT tours, note this now exports every tour in the open model.
    public List<TourDTO> exportTours() {
        logger.info("Exporting all tours");
        return getAllTours();
    }

    // IMPORT tours
    public void importTours(List<TourDTO> tourDtos) {
        logger.info("Importing {} tours", tourDtos.size());

        for (TourDTO dto : tourDtos) {
            dto.setId(null); // imported tours should be created as new database rows
            createTour(dto);
        }
    }



    // --- Helper: convert Entity → DTO ---
    private TourDTO toDTO(Tour tour) {
        TourDTO dto = new TourDTO();
        dto.setId(tour.getId());
        dto.setOwnerUsername(tour.getUser().getUsername());
        dto.setName(tour.getName());
        dto.setDescription(tour.getDescription());
        dto.setFromLocation(tour.getFromLocation());
        dto.setToLocation(tour.getToLocation());
        dto.setTransportType(tour.getTransportType());
        dto.setDistance(tour.getDistance());
        dto.setEstimatedTime(tour.getEstimatedTime());
        dto.setRouteInformation(tour.getRouteInformation());
        // Computed: popularity score (0–100) and level label
        List<com.tourplanner.entity.TourLog> logs = tourLogRepository.findByTourId(tour.getId());
        int popularityScore = computePopularityScore(logs);
        dto.setPopularity(popularityScore);
        dto.setPopularityLevel(computePopularityLevel(popularityScore, logs.isEmpty()));
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

    // Popularity score (0–100): 70% from avg rating (1–5 scale), 30% from log volume (caps at 10 logs)
    private int computePopularityScore(List<com.tourplanner.entity.TourLog> logs) {
        if (logs.isEmpty()) return 0;
        double avgRating = logs.stream()
                .mapToInt(log -> log.getRating() != null ? log.getRating() : 1)
                .average()
                .orElse(1);
        double ratingComponent  = (avgRating - 1.0) / 4.0 * 70.0;
        double volumeComponent  = Math.min(logs.size() / 10.0, 1.0) * 30.0;
        return (int) Math.round(ratingComponent + volumeComponent);
    }

    private String computePopularityLevel(int score, boolean noLogs) {
        if (noLogs)      return "Unknown";
        if (score <= 20) return "Not Recommended";
        if (score <= 40) return "Hidden Gem";
        if (score <= 60) return "Rising Star";
        if (score <= 80) return "Popular";
        return "Legendary";
    }

    // Derived attribute: child-friendliness
    // Maps average log difficulty to a rating: Easy=1, Medium=2, Hard=3
    // avg <= 1.5 → Child Friendly, <= 2.5 → Moderate, > 2.5 → Not Child Friendly
    private String computeChildFriendliness(Tour tour) {
        List<com.tourplanner.entity.TourLog> logs = tourLogRepository.findByTourId(tour.getId());
        if (logs.isEmpty()) return "Unknown";

        double avg = logs.stream()
                .mapToInt(log -> switch (log.getDifficulty()) {
                    case Easy   -> 1;
                    case Medium -> 2;
                    case Hard   -> 3;
                })
                .average()
                .orElse(0);

        if (avg <= 1.5) return "Child Friendly";
        if (avg <= 2.5) return "Moderate";
        return "Not Child Friendly";
    }
}