package com.tourplanner.repository;

import com.tourplanner.entity.TourLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TourLogRepository extends JpaRepository<TourLog, Long> {

    // Spring generates this SQL automatically from the method name:
    // SELECT * FROM tour_logs WHERE tour_id = ?
    List<TourLog> findByTourId(Long tourId);

    // Count logs for a tour (used to compute "popularity")
    long countByTourId(Long tourId);
}