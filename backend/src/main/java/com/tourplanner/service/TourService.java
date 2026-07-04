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

// this is the kitchen for tours, where the real work happens.
// the controller takes orders at the counter and passes them here. this class talks to the
// database, asks the route service to work out distances, checks who owns what, and turns
// database rows into the tidy shapes the frontend gets back.
// it also works out the extra bits like how popular a tour is and how child friendly it is.
@Service
public class TourService {

    // the logger writes notes into the log file so we can see later what the app did
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

    // get every tour and turn each one into the tidy shape the frontend wants
    public List<TourDTO> getAllTours() {
        logger.info("Fetching all tours");

        return tourRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // get one tour by its id, or complain with a not found error if there is no such tour
    public TourDTO getTourById(Long id) {
        logger.info("Fetching tour with id {}", id);

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found: " + id));

        return toDTO(tour);
    }

    // make a brand new tour.
    // step by step: build the tour, stamp the current user as its owner, then try to look up
    // the real distance and time from the map service. if the map service is down we just keep
    // the values the user typed, so a new tour still saves either way.
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

    // change an existing tour.
    // first we find it, then we check it really belongs to the person asking (you cannot edit
    // someone else's tour), then we copy over the new details and refresh the route if we can.
    public TourDTO updateTour(Long id, TourDTO dto) {
        logger.info("Updating tour id {}", id);

        Tour existing = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found: " + id));

        // check the name tag: only the owner is allowed past this point
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

    // remove a tour, but only if it belongs to the person asking
    public void deleteTour(Long id) {
        logger.info("Deleting tour id {}", id);

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour not found: " + id));

        // check the name tag: only the owner may delete it
        if (!tour.getUser().getId().equals(currentUser.get().getId())) {
            throw new ForbiddenException("This tour is not yours");
        }

        tourRepository.deleteById(id);
    }

    // search in two passes. the database finds matches in the stored fields (name,
    // description, locations, log comments). but popularity and child friendliness are
    // worked out on the fly and never stored, so the database cannot see them - for those
    // we build the finished dto for every tour and check the computed values ourselves.
    public List<TourDTO> searchTours(String query) {
        logger.info("Searching tours with query: {}", query);

        String q = query == null ? "" : query.toLowerCase().trim();

        // pass 1: what the database can find in the stored columns
        List<TourDTO> results = tourRepository.searchTours(query)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        java.util.Set<Long> alreadyFound = results.stream()
                .map(TourDTO::getId)
                .collect(Collectors.toSet());

        // pass 2: what only we can find, in the computed values
        for (Tour tour : tourRepository.findAll()) {
            if (alreadyFound.contains(tour.getId())) {
                continue; // already in the result list, skip it
            }
            TourDTO dto = toDTO(tour);
            if (matchesComputedValues(dto, q)) {
                results.add(dto);
            }
        }

        return results;
    }

    // does the search word match one of the values we calculate ourselves?
    private boolean matchesComputedValues(TourDTO dto, String q) {
        if (q.isBlank()) {
            return false;
        }
        return (dto.getPopularityLevel() != null
                    && dto.getPopularityLevel().toLowerCase().contains(q))
            || (dto.getChildFriendliness() != null
                    && dto.getChildFriendliness().toLowerCase().contains(q))
            || String.valueOf(dto.getPopularity()).equals(q);
    }

    // export: gather every tour together with all of its logs, ready to be saved as a file.
    // we use the "with logs" shape here so the whole dataset can be restored later.
    public List<TourDTO> exportTours() {
        logger.info("Exporting all tours with logs");

        return tourRepository.findAll()
                .stream()
                .map(this::toDTOWithLogs)
                .collect(Collectors.toList());
    }

    // import: take a file full of saved tours and add them all back in.
    // step by step: for each tour we clear its old id so the database gives it a fresh one,
    // save the tour, then save each of its logs and stamp the current user as the owner.
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

    // turn a tour from the database into the tidy shape the frontend gets.
    // along the way we also count up its logs to work out popularity and child friendliness.
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

    // same as the tidy shape above, but with all the tour's logs tucked in too, used for export
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

    // go the other way: turn the shape the frontend sent us into a tour ready for the database
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

    // turn one log from the database into the tidy shape used inside an exported tour
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

    // go the other way for a log: turn an imported log shape into a log ready for the database
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

    // work out a popularity score from 0 to 100 for a tour, based on its logs.
    // think of it like a report card built from two marks:
    // most of the score (up to 70) comes from the average star rating people gave,
    // and the rest (up to 30) comes from how many logs there are, so a well loved tour that
    // lots of people have done scores highest. a tour with no logs scores zero.
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

    // turn that 0 to 100 number into a friendly label, like turning a percentage into a grade
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

    // how child friendly is this tour? the requirement says to weigh three things from the
    // logs: how hard people found it, how long it took, and how far it was. each factor is
    // scored 1 (fine for kids) to 3 (not for kids), and the average of the factors we
    // actually have decides the label. a factor that is missing simply does not count,
    // so a tour with only difficulty recorded is judged on difficulty alone, like before.
    private String computeChildFriendliness(Tour tour) {
        List<TourLog> logs = tourLogRepository.findByTourId(tour.getId());

        if (logs.isEmpty()) {
            return "Unknown";
        }

        double sum = 0;
        int factors = 0;

        // factor 1: difficulty. Easy=1, Medium=2, Hard=3
        double avgDifficulty = logs.stream()
                .mapToInt(log -> switch (log.getDifficulty()) {
                    case Easy -> 1;
                    case Medium -> 2;
                    case Hard -> 3;
                })
                .average()
                .orElse(0);
        sum += avgDifficulty;
        factors++;

        // factor 2: distance. up to 5 km is fine for kids, up to 15 km is a stretch,
        // anything longer is not a childrens trip
        double avgDistance = logs.stream()
                .filter(log -> log.getTotalDistance() != null)
                .mapToDouble(TourLog::getTotalDistance)
                .average()
                .orElse(-1);
        if (avgDistance >= 0) {
            sum += avgDistance <= 5 ? 1 : (avgDistance <= 15 ? 2 : 3);
            factors++;
        }

        // factor 3: time. up to 2 hours is fine, up to 5 hours is a stretch, longer is not.
        // totalTime is free text, so we only count it when we can actually read a duration
        double avgMinutes = logs.stream()
                .map(log -> parseMinutes(log.getTotalTime()))
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(-1);
        if (avgMinutes >= 0) {
            sum += avgMinutes <= 120 ? 1 : (avgMinutes <= 300 ? 2 : 3);
            factors++;
        }

        double score = sum / factors;

        if (score <= 1.5) {
            return "Child Friendly";
        }
        if (score <= 2.5) {
            return "Moderate";
        }
        return "Not Child Friendly";
    }

    // try to read a duration in minutes out of free text like "2h 30m", "90 min" or "1:30".
    // returns null when the text cannot be understood, so bad input never skews the score.
    private Double parseMinutes(String totalTime) {
        if (totalTime == null || totalTime.isBlank()) {
            return null;
        }
        String t = totalTime.toLowerCase().trim();

        // "1:30" style -> hours:minutes
        java.util.regex.Matcher clock = java.util.regex.Pattern
                .compile("^(\\d{1,2}):(\\d{2})$").matcher(t);
        if (clock.matches()) {
            return Double.parseDouble(clock.group(1)) * 60
                + Double.parseDouble(clock.group(2));
        }

        double minutes = 0;
        boolean found = false;

        java.util.regex.Matcher hours = java.util.regex.Pattern
                .compile("(\\d+(?:\\.\\d+)?)\\s*h").matcher(t);
        if (hours.find()) {
            minutes += Double.parseDouble(hours.group(1)) * 60;
            found = true;
        }

        java.util.regex.Matcher mins = java.util.regex.Pattern
                .compile("(\\d+)\\s*m").matcher(t);
        if (mins.find()) {
            minutes += Double.parseDouble(mins.group(1));
            found = true;
        }

        // a bare number like "90" is read as minutes
        if (!found && t.matches("\\d+")) {
            return Double.parseDouble(t);
        }

        return found ? minutes : null;
    }
}