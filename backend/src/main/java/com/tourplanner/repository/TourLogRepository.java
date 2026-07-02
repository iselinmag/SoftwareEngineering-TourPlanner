package com.tourplanner.repository;

import com.tourplanner.entity.TourLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// this is the drawer where tour logs are stored.
// spring gives us the everyday actions for free, so we only add the two lookups we need.
// the trick here is the method name itself, spring reads the name and writes the query for us.
@Repository
public interface TourLogRepository extends JpaRepository<TourLog, Long> {

    // get every log that belongs to one tour
    List<TourLog> findByTourId(Long tourId);

    // count how many logs a tour has, used to work out how popular a tour is
    long countByTourId(Long tourId);
}