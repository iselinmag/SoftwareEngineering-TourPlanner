package com.tourplanner.service;

import com.tourplanner.dto.TourDTO;
import com.tourplanner.dto.TourLogDTO;
import com.tourplanner.entity.Tour;
import com.tourplanner.entity.TourLog;
import com.tourplanner.exception.ForbiddenException;
import com.tourplanner.exception.NotFoundException;
import com.tourplanner.repository.TourLogRepository;
import com.tourplanner.repository.TourRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TourService {

    private static final Logger logger = LogManager.getLogger(TourService.class);

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

    public List<TourDTO> getAllTours() {
        logger.info("Fetching all tours");

        return tourRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TourDTO getTourById(Long id) {
        logger.info("Fetching tour with id {}", id);

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found: " + id));

        return toDTO(tour);
    }

    public TourDTO createTour(TourDTO dto) {
        logger.info("Creating tour: {}", dto.getName());

        Tour tour = toEntity(dto);
        tour.setUser(currentUser.get());

        try {
            RouteService.RouteResult route = routeService.getRoute(
                    dto.getFromLocation(),
                    dto.getToLocation(),
                    dto.getTransportType().name()
            );

            if (route != null) {
                tour.setDistance(route.distanceKm);
                tour.setEstimatedTime(route.estimatedTime);
                tour.setRouteInformation(route.geometryJson);
                logger.info("Route data applied: {} km, {}", route.distanceKm, route.estimatedTime);
            } else {
                logger.warn("Route fetch failed, using user-provided values");
            }
        } catch (Exception e) {
            logger.error("Route service threw an exception, saving tour with user values: {}", e.getMessage());
        }

        return toDTO(tourRepository.save(tour));
    }

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
                existing.setDistance(dto.getDistance());
                existing.setEstimatedTime(dto.getEstimatedTime());
            }
        } catch (Exception e) {
            logger.error("Route service threw an exception, saving tour with user values: {}", e.getMessage());
        }

        return toDTO(tourRepository.save(existing));
    }

    public void deleteTour(Long id) {
        logger.info("Deleting tour id {}", id);

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found: " + id));

        if (!tour.getUser().getId().equals(currentUser.get().getId())) {
            throw new ForbiddenException("This tour is not yours");
        }

        tourRepository.deleteById(id);
    }

    public List<TourDTO> searchTours(String query) {
        logger.info("Searching tours with query: {}", query);

        return tourRepository.searchTours(query)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // EXPORT tours including their tour logs
    public List<TourDTO> exportTours() {
        logger.info("Exporting all tours with logs");

        return tourRepository.findAll()
                .stream()
                .map(this::toDTOWithLogs)
                .collect(Collectors.toList());
    }

    // IMPORT tours including their tour logs
    public void importTours(List<TourDTO> tourDtos) {
        logger.info("Importing {} tours with logs", tourDtos.size());

        for (TourDTO dto : tourDtos) {
            dto.setId(null);

            TourDTO createdTourDto = createTour(dto);

            Tour createdTour = tourRepository.findById(createdTourDto.getId())
                    .orElseThrow(() -> new NotFoundException(
                            "Imported tour not found after save: " + createdTourDto.getId()
                    ));

            if (dto.getTourLogs() != null) {
                for (TourLogDTO logDto : dto.getTourLogs()) {
                    logDto.setId(null);

                    TourLog log = toLogEntity(logDto, createdTour);
                    log.setUser(currentUser.get());

                    tourLogRepository.save(log);
                }
            }
        }
    }

    private TourDTO toDTO(Tour tour) {
        TourDTO dto = new TourDTO();

        dto.setId(tour.getId());
        dto.setOwnerUsername(tour.getUser() != null ? tour.getUser().getUsername() : null);
        dto.setName(tour.getName());
        dto.setDescription(tour.getDescription());
        dto.setFromLocation(tour.getFromLocation());
        dto.setToLocation(tour.getToLocation());
        dto.setTransportType(tour.getTransportType());
        dto.setDistance(tour.getDistance());
        dto.setEstimatedTime(tour.getEstimatedTime());
        dto.setRouteInformation(tour.getRouteInformation());

        List<TourLog> logs = tourLogRepository.findByTourId(tour.getId());

        int popularityScore = computePopularityScore(logs);
        dto.setPopularity(popularityScore);
        dto.setPopularityLevel(computePopularityLevel(popularityScore, logs.isEmpty()));
        dto.setChildFriendliness(computeChildFriendliness(tour));

        return dto;
    }

    private TourDTO toDTOWithLogs(Tour tour) {
        TourDTO dto = toDTO(tour);

        List<TourLog> logs = tourLogRepository.findByTourId(tour.getId());

        dto.setTourLogs(
                logs.stream()
                        .map(this::toLogDTO)
                        .collect(Collectors.toList())
        );

        return dto;
    }

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

    private TourLogDTO toLogDTO(TourLog log) {
        TourLogDTO dto = new TourLogDTO();

        dto.setId(log.getId());
        dto.setOwnerUsername(log.getUser() != null ? log.getUser().getUsername() : null);
        dto.setTourId(log.getTour() != null ? log.getTour().getId() : null);
        dto.setDateTime(log.getDateTime());
        dto.setComment(log.getComment());
        dto.setDifficulty(log.getDifficulty());
        dto.setTotalDistance(log.getTotalDistance());
        dto.setTotalTime(log.getTotalTime());
        dto.setRating(log.getRating());

        return dto;
    }

    private TourLog toLogEntity(TourLogDTO dto, Tour tour) {
        TourLog log = new TourLog();

        log.setTour(tour);
        log.setDateTime(dto.getDateTime());
        log.setComment(dto.getComment());
        log.setDifficulty(dto.getDifficulty());
        log.setTotalDistance(dto.getTotalDistance());
        log.setTotalTime(dto.getTotalTime());
        log.setRating(dto.getRating());

        return log;
    }

    private int computePopularityScore(List<TourLog> logs) {
        if (logs.isEmpty()) {
            return 0;
        }

        double avgRating = logs.stream()
                .mapToInt(log -> log.getRating() != null ? log.getRating() : 1)
                .average()
                .orElse(1);

        double ratingComponent = (avgRating - 1.0) / 4.0 * 70.0;
        double volumeComponent = Math.min(logs.size() / 10.0, 1.0) * 30.0;

        return (int) Math.round(ratingComponent + volumeComponent);
    }

    private String computePopularityLevel(int score, boolean noLogs) {
        if (noLogs) {
            return "Unknown";
        }

        if (score <= 20) {
            return "Not Recommended";
        }

        if (score <= 40) {
            return "Hidden Gem";
        }

        if (score <= 60) {
            return "Rising Star";
        }

        if (score <= 80) {
            return "Popular";
        }

        return "Legendary";
    }

    private String computeChildFriendliness(Tour tour) {
        List<TourLog> logs = tourLogRepository.findByTourId(tour.getId());

        if (logs.isEmpty()) {
            return "Unknown";
        }

        double avgDifficulty = logs.stream()
                .mapToInt(log -> switch (log.getDifficulty()) {
                    case Easy -> 1;
                    case Medium -> 2;
                    case Hard -> 3;
                })
                .average()
                .orElse(0);

        if (avgDifficulty <= 1.5) {
            return "Child Friendly";
        }

        if (avgDifficulty <= 2.5) {
            return "Moderate";
        }

        return "Not Child Friendly";
    }
}