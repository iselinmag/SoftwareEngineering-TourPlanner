package com.tourplanner.service;

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
public class TourLogService {

    private static final Logger logger = LogManager.getLogger(TourLogService.class);

    private final TourLogRepository tourLogRepository;
    private final TourRepository tourRepository;
    private final CurrentUser currentUser;

    public TourLogService(TourLogRepository tourLogRepository,
                          TourRepository tourRepository,
                          CurrentUser currentUser) {
        this.tourLogRepository = tourLogRepository;
        this.tourRepository = tourRepository;
        this.currentUser = currentUser;
    }

    // GET all logs for a specific tour, anyone can read all logs in the open model
    public List<TourLogDTO> getLogsForTour(Long tourId) {
        logger.info("Fetching logs for tour {}", tourId);
        return tourLogRepository.findByTourId(tourId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // CREATE log, anyone can add a log to any tour, we just record who wrote it
    public TourLogDTO createLog(TourLogDTO dto) {
        logger.info("Creating log for tour {}", dto.getTourId());
        Tour tour = tourRepository.findById(dto.getTourId())
                .orElseThrow(() -> new NotFoundException("Tour not found: " + dto.getTourId()));
        TourLog log = toEntity(dto, tour);
        log.setUser(currentUser.get());   // stamp the author of this log
        return toDTO(tourLogRepository.save(log));
    }

    // UPDATE log, only the author of the log may edit it
    public TourLogDTO updateLog(Long id, TourLogDTO dto) {
        logger.info("Updating log id {}", id);
        TourLog existing = tourLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Log not found: " + id));

        // the guard reading the name tag on the log itself
        if (!existing.getUser().getId().equals(currentUser.get().getId())) {
            throw new ForbiddenException("This log is not yours");
        }

        existing.setDateTime(dto.getDateTime());
        existing.setComment(dto.getComment());
        existing.setDifficulty(dto.getDifficulty());
        existing.setTotalDistance(dto.getTotalDistance());
        existing.setTotalTime(dto.getTotalTime());
        existing.setRating(dto.getRating());
        return toDTO(tourLogRepository.save(existing));
    }

    // DELETE log, only the author of the log may delete it
    public void deleteLog(Long id) {
        logger.info("Deleting log id {}", id);

        // load the log first so we can check who wrote it
        TourLog existing = tourLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Log not found: " + id));

        // the guard reading the name tag on the log itself
        if (!existing.getUser().getId().equals(currentUser.get().getId())) {
            throw new ForbiddenException("This log is not yours");
        }

        tourLogRepository.deleteById(id);
    }

    private TourLogDTO toDTO(TourLog log) {
        TourLogDTO dto = new TourLogDTO();
        dto.setId(log.getId());
        dto.setOwnerUsername(log.getUser().getUsername());
        dto.setTourId(log.getTour().getId());
        dto.setDateTime(log.getDateTime());
        dto.setComment(log.getComment());
        dto.setDifficulty(log.getDifficulty());
        dto.setTotalDistance(log.getTotalDistance());
        dto.setTotalTime(log.getTotalTime());
        dto.setRating(log.getRating());
        return dto;
    }

    private TourLog toEntity(TourLogDTO dto, Tour tour) {
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
}
