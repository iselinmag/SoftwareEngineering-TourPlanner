package com.tourplanner.service;

import com.tourplanner.dto.TourLogDTO;
import com.tourplanner.entity.Tour;
import com.tourplanner.entity.TourLog;
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

    public TourLogService(TourLogRepository tourLogRepository, TourRepository tourRepository) {
        this.tourLogRepository = tourLogRepository;
        this.tourRepository = tourRepository;
    }

    // GET all logs for a specific tour
    public List<TourLogDTO> getLogsForTour(Long tourId) {
        logger.info("Fetching logs for tour {}", tourId);
        return tourLogRepository.findByTourId(tourId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // CREATE log
    public TourLogDTO createLog(TourLogDTO dto) {
        logger.info("Creating log for tour {}", dto.getTourId());
        Tour tour = tourRepository.findById(dto.getTourId())
                .orElseThrow(() -> new RuntimeException("Tour not found: " + dto.getTourId()));
        TourLog log = toEntity(dto, tour);
        return toDTO(tourLogRepository.save(log));
    }

    // UPDATE log
    public TourLogDTO updateLog(Long id, TourLogDTO dto) {
        logger.info("Updating log id {}", id);
        TourLog existing = tourLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found: " + id));
        existing.setDateTime(dto.getDateTime());
        existing.setComment(dto.getComment());
        existing.setDifficulty(dto.getDifficulty());
        existing.setTotalDistance(dto.getTotalDistance());
        existing.setTotalTime(dto.getTotalTime());
        existing.setRating(dto.getRating());
        return toDTO(tourLogRepository.save(existing));
    }

    // DELETE log
    public void deleteLog(Long id) {
        logger.info("Deleting log id {}", id);
        tourLogRepository.deleteById(id);
    }

    private TourLogDTO toDTO(TourLog log) {
        TourLogDTO dto = new TourLogDTO();
        dto.setId(log.getId());
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