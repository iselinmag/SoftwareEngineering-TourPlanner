package com.tourplanner.service;

import com.tourplanner.dto.TourLogDTO;
import com.tourplanner.entity.Tour;
import com.tourplanner.entity.TourLog;
import com.tourplanner.entity.User;
import com.tourplanner.repository.TourLogRepository;
import com.tourplanner.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourLogServiceTest {

    @Mock
    private TourLogRepository tourLogRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private CurrentUser currentUser;

    // the service also needs the file storage helper for image uploads
    @Mock
    private FileStorageService fileStorageService;

    private TourLogService tourLogService;

    @BeforeEach
    void setUp() {
        tourLogService = new TourLogService(tourLogRepository, tourRepository, currentUser, fileStorageService);
    }

    @Test
    void getLogsForTour_returnsLogsAsDtos() {
        Tour tour = sampleTour(1L);
        TourLog log = sampleLog(10L, tour);

        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of(log));

        List<TourLogDTO> result = tourLogService.getLogsForTour(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getTourId()).isEqualTo(1L);
    }

    @Test
    void getLogsForTour_whenNoLogs_returnsEmptyList() {
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of());

        List<TourLogDTO> result = tourLogService.getLogsForTour(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void createLog_whenTourExists_savesLog() {
        Tour tour = sampleTour(1L);
        TourLogDTO input = sampleDto(1L);

        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        // NEW: createLog stamps the author via currentUser.get()
        when(currentUser.get()).thenReturn(sampleUser(1L));

        when(tourLogRepository.save(any(TourLog.class))).thenAnswer(invocation -> {
            TourLog saved = invocation.getArgument(0);
            saved.setId(20L);
            return saved;
        });

        TourLogDTO result = tourLogService.createLog(input);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getTourId()).isEqualTo(1L);
        assertThat(result.getComment()).isEqualTo("Nice trip");
    }

    @Test
    void createLog_whenTourDoesNotExist_throwsException() {
        TourLogDTO input = sampleDto(99L);

        when(tourRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourLogService.createLog(input))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tour not found: 99");
    }

    @Test
    void createLog_setsCorrectTourRelationBeforeSaving() {
        Tour tour = sampleTour(1L);
        TourLogDTO input = sampleDto(1L);

        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(currentUser.get()).thenReturn(sampleUser(1L));
        when(tourLogRepository.save(any(TourLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tourLogService.createLog(input);

        ArgumentCaptor<TourLog> captor = ArgumentCaptor.forClass(TourLog.class);
        verify(tourLogRepository).save(captor.capture());

        assertThat(captor.getValue().getTour()).isEqualTo(tour);
    }

    @Test
    void updateLog_whenLogExists_updatesFields() {
        Tour tour = sampleTour(1L);
        TourLog existing = sampleLog(10L, tour);

        TourLogDTO update = sampleDto(1L);
        update.setComment("Updated comment");
        update.setDifficulty(TourLog.Difficulty.Hard);
        update.setRating(2);

        when(tourLogRepository.findById(10L)).thenReturn(Optional.of(existing));
        // NEW: updateLog checks ownership; current user id must match the log author id (1L)
        when(currentUser.get()).thenReturn(sampleUser(1L));
        when(tourLogRepository.save(any(TourLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TourLogDTO result = tourLogService.updateLog(10L, update);

        assertThat(result.getComment()).isEqualTo("Updated comment");
        assertThat(result.getDifficulty()).isEqualTo(TourLog.Difficulty.Hard);
        assertThat(result.getRating()).isEqualTo(2);
    }

    @Test
    void updateLog_whenLogDoesNotExist_throwsException() {
        when(tourLogRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourLogService.updateLog(404L, sampleDto(1L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Log not found: 404");
    }

    @Test
    void deleteLog_whenAuthor_callsRepositoryDeleteById() {
        // NEW: deleteLog now loads the log and verifies ownership before deleting,
        // so we must stub findById and currentUser, then verify deleteById is reached.
        Tour tour = sampleTour(1L);
        TourLog existing = sampleLog(10L, tour);
        when(tourLogRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(currentUser.get()).thenReturn(sampleUser(1L));

        tourLogService.deleteLog(10L);

        verify(tourLogRepository).deleteById(10L);
    }

    @Test
    void toDto_mapsDateDistanceTimeDifficultyAndRating() {
        Tour tour = sampleTour(1L);
        TourLog log = sampleLog(10L, tour);

        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of(log));

        TourLogDTO result = tourLogService.getLogsForTour(1L).get(0);

        assertThat(result.getDateTime()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(result.getTotalDistance()).isEqualTo(6.5);
        assertThat(result.getTotalTime()).isEqualTo("01:10");
        assertThat(result.getDifficulty()).isEqualTo(TourLog.Difficulty.Medium);
        assertThat(result.getRating()).isEqualTo(4);
    }

    // NEW: helper that builds the logged-in user used for author stamping / ownership checks
    private User sampleUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("tester");
        return user;
    }

    private Tour sampleTour(Long id) {
        Tour tour = new Tour();
        tour.setId(id);
        tour.setName("Vienna Tour");
        return tour;
    }

    private TourLog sampleLog(Long id, Tour tour) {
        TourLog log = new TourLog();
        log.setId(id);
        log.setTour(tour);
        log.setDateTime(LocalDate.of(2026, 6, 1));
        log.setComment("Nice trip");
        log.setDifficulty(TourLog.Difficulty.Medium);
        log.setTotalDistance(6.5);
        log.setTotalTime("01:10");
        log.setRating(4);
        // NEW: toDTO reads log.getUser().getUsername(), so the author must be set
        log.setUser(sampleUser(1L));
        return log;
    }

    private TourLogDTO sampleDto(Long tourId) {
        TourLogDTO dto = new TourLogDTO();
        dto.setTourId(tourId);
        dto.setDateTime(LocalDate.of(2026, 6, 1));
        dto.setComment("Nice trip");
        dto.setDifficulty(TourLog.Difficulty.Medium);
        dto.setTotalDistance(6.5);
        dto.setTotalTime("01:10");
        dto.setRating(4);
        return dto;
    }
}