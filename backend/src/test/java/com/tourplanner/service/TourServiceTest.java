package com.tourplanner.service;

import com.tourplanner.dto.TourDTO;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.tourplanner.exception.ForbiddenException;

// these are the tests for the tour kitchen (TourService).
// a test checks that the code does what we expect without needing a real database or the
// real map service. instead we hand the service stand ins we control (mocks, fake versions
// of the real parts) and then check it reacts the right way. each method below is one
// small scene we set up and then check the ending of.
@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    // fake versions of the parts the service leans on, so we can steer what they return
    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourLogRepository tourLogRepository;

    @Mock
    private RouteService routeService;

    // the service needs to know who is logged in, so we hand it a fake current user too
    @Mock
    private CurrentUser currentUser;

    private TourService tourService;

    // runs before each test, builds a fresh service wired up to the fakes above
    @BeforeEach
    void setUp() {
        tourService = new TourService(tourRepository, tourLogRepository, routeService, currentUser);
    }

    @Test
    void getAllTours_returnsAllToursAsDtos() {
        Tour tour = sampleTour(1L, "Vienna Walk");
        when(tourRepository.findAll()).thenReturn(List.of(tour));
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of());

        List<TourDTO> result = tourService.getAllTours();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Vienna Walk");
    }

    @Test
    void getTourById_whenTourExists_returnsTourDto() {
        Tour tour = sampleTour(1L, "City Tour");
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of());

        TourDTO result = tourService.getTourById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("City Tour");
    }

    @Test
    void getTourById_whenTourDoesNotExist_throwsException() {
        when(tourRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourService.getTourById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tour not found: 99");
    }

    @Test
    void createTour_whenRouteServiceReturnsRoute_savesTourWithRouteData() {
        TourDTO input = sampleDto("Museum Trip");

        // when the service asks who is logged in, hand back our test user
        when(currentUser.get()).thenReturn(sampleUser(1L));

        RouteService.RouteResult routeResult =
                new RouteService.RouteResult(
                        12.5,
                        "01:30",
                        "[[16.1,48.1],[16.2,48.2]]"
                );

        when(routeService.getRoute("Vienna", "Prater", "Walk")).thenReturn(routeResult);

        when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> {
            Tour saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        when(tourLogRepository.findByTourId(10L)).thenReturn(List.of());

        TourDTO result = tourService.createTour(input);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getDistance()).isEqualTo(12.5);
        assertThat(result.getEstimatedTime()).isEqualTo("01:30");
        assertThat(result.getRouteInformation()).isEqualTo("[[16.1,48.1],[16.2,48.2]]");
    }

    @Test
    void createTour_whenRouteServiceReturnsNull_keepsUserProvidedDistanceAndTime() {
        TourDTO input = sampleDto("Manual Route");
        input.setDistance(5.0);
        input.setEstimatedTime("00:45");

        when(currentUser.get()).thenReturn(sampleUser(1L));
        when(routeService.getRoute("Vienna", "Prater", "Walk")).thenReturn(null);

        when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> {
            Tour saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        when(tourLogRepository.findByTourId(11L)).thenReturn(List.of());

        TourDTO result = tourService.createTour(input);

        assertThat(result.getDistance()).isEqualTo(5.0);
        assertThat(result.getEstimatedTime()).isEqualTo("00:45");
    }

    @Test
    void createTour_whenRouteServiceThrowsException_stillSavesTour() {
        TourDTO input = sampleDto("Fallback Route");
        input.setDistance(8.0);
        input.setEstimatedTime("01:00");

        when(currentUser.get()).thenReturn(sampleUser(1L));
        when(routeService.getRoute("Vienna", "Prater", "Walk"))
                .thenThrow(new RuntimeException("API down"));

        when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> {
            Tour saved = invocation.getArgument(0);
            saved.setId(12L);
            return saved;
        });

        when(tourLogRepository.findByTourId(12L)).thenReturn(List.of());

        TourDTO result = tourService.createTour(input);

        assertThat(result.getId()).isEqualTo(12L);
        assertThat(result.getDistance()).isEqualTo(8.0);
        assertThat(result.getEstimatedTime()).isEqualTo("01:00");
    }

    @Test
    void updateTour_whenTourExists_updatesBasicFields() {
        Tour existing = sampleTour(1L, "Old name");

        TourDTO update = sampleDto("New name");
        update.setDescription("New description");

        when(tourRepository.findById(1L)).thenReturn(Optional.of(existing));
        // the tour is owned by user 1, so we log in as user 1 to be allowed to change it
        when(currentUser.get()).thenReturn(sampleUser(1L));
        when(routeService.getRoute("Vienna", "Prater", "Walk")).thenReturn(null);
        when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of());

        TourDTO result = tourService.updateTour(1L, update);

        assertThat(result.getName()).isEqualTo("New name");
        assertThat(result.getDescription()).isEqualTo("New description");
    }

    @Test
    void updateTour_whenTourDoesNotExist_throwsException() {
        when(tourRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourService.updateTour(404L, sampleDto("Missing")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tour not found: 404");
    }

    @Test
    void deleteTour_whenOwner_callsRepositoryDeleteById() {
        // deleting first checks the tour exists and belongs to you, so we set up both,
        // then check the delete actually reaches the database
        Tour existing = sampleTour(5L, "To delete");
        when(tourRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(currentUser.get()).thenReturn(sampleUser(1L));

        tourService.deleteTour(5L);

        verify(tourRepository).deleteById(5L);
    }

    @Test
    void updateTour_whenNotOwner_throwsForbidden() {
        // this tour belongs to user 1 (sampleTour always makes user 1 the owner)
        Tour existing = sampleTour(1L, "someone elses tour");
        when(tourRepository.findById(1L)).thenReturn(Optional.of(existing));

        // but the person asking right now is user 2, a different person
        when(currentUser.get()).thenReturn(sampleUser(2L));

        // so trying to edit it should be blocked with a not yours error
        assertThatThrownBy(() -> tourService.updateTour(1L, sampleDto("hijacked name")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not yours");

        // the important part: the change must never reach the save step
        verify(tourRepository, never()).save(any());
    }

    @Test
    void deleteTour_whenNotOwner_throwsForbiddenAndDoesNotDelete() {
        // again the tour is user 1's, but user 2 is the one asking
        Tour existing = sampleTour(5L, "not yours to delete");
        when(tourRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(currentUser.get()).thenReturn(sampleUser(2L));

        assertThatThrownBy(() -> tourService.deleteTour(5L))
                .isInstanceOf(ForbiddenException.class);

        // the delete must never actually happen
        verify(tourRepository, never()).deleteById(any());
    }

    @Test
    void searchTours_returnsMatchingToursAsDtos() {
        Tour tour = sampleTour(2L, "Bike Tour");

        when(tourRepository.searchTours("bike")).thenReturn(List.of(tour));
        when(tourLogRepository.findByTourId(2L)).thenReturn(List.of());

        List<TourDTO> result = tourService.searchTours("bike");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Bike Tour");
    }

    @Test
    void toDto_whenNoLogs_setsPopularityZeroAndUnknownLabels() {
        Tour tour = sampleTour(3L, "No logs");

        when(tourRepository.findById(3L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByTourId(3L)).thenReturn(List.of());

        TourDTO result = tourService.getTourById(3L);

        assertThat(result.getPopularity()).isZero();
        assertThat(result.getPopularityLevel()).isEqualTo("Unknown");
        assertThat(result.getChildFriendliness()).isEqualTo("Unknown");
    }

    @Test
    void popularityLevel_whenLowScore_isNotRecommended() {
        Tour tour = sampleTour(4L, "Low rating");

        when(tourRepository.findById(4L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByTourId(4L)).thenReturn(List.of(
                log(TourLog.Difficulty.Easy, 1, 2.0, "00:30")
        ));

        TourDTO result = tourService.getTourById(4L);

        assertThat(result.getPopularityLevel()).isEqualTo("Not Recommended");
    }

    @Test
    void popularityLevel_whenMediumScore_isRisingStar() {
        Tour tour = sampleTour(5L, "Medium rating");

        when(tourRepository.findById(5L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByTourId(5L)).thenReturn(List.of(
                log(TourLog.Difficulty.Medium, 3, 5.0, "01:00"),
                log(TourLog.Difficulty.Medium, 3, 5.0, "01:00")
        ));

        TourDTO result = tourService.getTourById(5L);

        assertThat(result.getPopularityLevel()).isEqualTo("Rising Star");
    }

    @Test
    void popularityLevel_whenHighScore_isLegendary() {
        Tour tour = sampleTour(6L, "High rating");

        List<TourLog> logs = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> log(TourLog.Difficulty.Easy, 5, 2.0, "00:30"))
                .toList();

        when(tourRepository.findById(6L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByTourId(6L)).thenReturn(logs);

        TourDTO result = tourService.getTourById(6L);

        assertThat(result.getPopularity()).isEqualTo(100);
        assertThat(result.getPopularityLevel()).isEqualTo("Legendary");
    }

    @Test
    void childFriendliness_whenEasyLogs_isChildFriendly() {
        Tour tour = sampleTour(7L, "Easy tour");

        when(tourRepository.findById(7L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByTourId(7L)).thenReturn(List.of(
                log(TourLog.Difficulty.Easy, 5, 2.0, "00:30"),
                log(TourLog.Difficulty.Easy, 4, 2.5, "00:40")
        ));

        TourDTO result = tourService.getTourById(7L);

        assertThat(result.getChildFriendliness()).isEqualTo("Child Friendly");
    }

    @Test
    void childFriendliness_whenMediumLogs_isModerate() {
        Tour tour = sampleTour(8L, "Medium tour");

        when(tourRepository.findById(8L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByTourId(8L)).thenReturn(List.of(
                log(TourLog.Difficulty.Medium, 4, 5.0, "01:00"),
                log(TourLog.Difficulty.Medium, 4, 5.0, "01:00")
        ));

        TourDTO result = tourService.getTourById(8L);

        assertThat(result.getChildFriendliness()).isEqualTo("Moderate");
    }

    @Test
    void childFriendliness_whenHardLogs_isNotChildFriendly() {
        Tour tour = sampleTour(9L, "Hard tour");

        when(tourRepository.findById(9L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByTourId(9L)).thenReturn(List.of(
                log(TourLog.Difficulty.Hard, 5, 20.0, "03:00"),
                log(TourLog.Difficulty.Hard, 4, 18.0, "02:30")
        ));

        TourDTO result = tourService.getTourById(9L);

        assertThat(result.getChildFriendliness()).isEqualTo("Not Child Friendly");
    }

    @Test
    void createTour_sendsCorrectEntityToRepository() {
        TourDTO input = sampleDto("Captured Tour");

        when(currentUser.get()).thenReturn(sampleUser(1L));
        when(routeService.getRoute("Vienna", "Prater", "Walk")).thenReturn(null);

        when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> {
            Tour saved = invocation.getArgument(0);
            saved.setId(13L);
            return saved;
        });

        when(tourLogRepository.findByTourId(13L)).thenReturn(List.of());

        tourService.createTour(input);

        ArgumentCaptor<Tour> captor = ArgumentCaptor.forClass(Tour.class);
        verify(tourRepository).save(captor.capture());

        assertThat(captor.getValue().getName()).isEqualTo("Captured Tour");
        assertThat(captor.getValue().getTransportType()).isEqualTo(Tour.TransportType.Walk);
    }

    // small helper that builds a test user, used as the owner in the scenes above
    private User sampleUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("tester");
        return user;
    }

    private Tour sampleTour(Long id, String name) {
        Tour tour = new Tour();
        tour.setId(id);
        tour.setName(name);
        tour.setDescription("Description");
        tour.setFromLocation("Vienna");
        tour.setToLocation("Prater");
        tour.setTransportType(Tour.TransportType.Walk);
        tour.setDistance(4.0);
        tour.setEstimatedTime("00:40");
        tour.setRouteInformation("[]");
        // the service reads the owner's username, so a test tour must have an owner set
        tour.setUser(sampleUser(1L));
        return tour;
    }

    private TourDTO sampleDto(String name) {
        TourDTO dto = new TourDTO();
        dto.setName(name);
        dto.setDescription("Description");
        dto.setFromLocation("Vienna");
        dto.setToLocation("Prater");
        dto.setTransportType(Tour.TransportType.Walk);
        dto.setDistance(4.0);
        dto.setEstimatedTime("00:40");
        dto.setRouteInformation("[]");
        return dto;
    }

    private TourLog log(TourLog.Difficulty difficulty, Integer rating, Double distance, String time) {
        TourLog log = new TourLog();
        log.setDifficulty(difficulty);
        log.setRating(rating);
        log.setTotalDistance(distance);
        log.setTotalTime(time);
        return log;
    }
}