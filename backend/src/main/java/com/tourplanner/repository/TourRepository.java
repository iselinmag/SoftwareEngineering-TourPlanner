package com.tourplanner.repository;

import com.tourplanner.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

// this is the drawer where tours are stored.
// spring already gives us the everyday actions for free and we never write those by hand:
// find all tours, find one by its id, save a tour, delete one and so on.
// below we add our own search, because that one is special.
@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

    // our own search. it takes one typed word and looks for it in the name, the description,
    // the from and to places, and even inside the tour log comments, so a match anywhere counts.
    // distinct makes sure a tour shows up only once even if the word matches in several spots.
    @Query("SELECT DISTINCT t FROM Tour t LEFT JOIN t.tourLogs l WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.fromLocation) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.toLocation) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.comment) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Tour> searchTours(String query);
}