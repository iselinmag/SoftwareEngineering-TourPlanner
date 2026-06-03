package com.tourplanner.repository;

import com.tourplanner.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    // JpaRepository<Tour, Long> means:
    //   Tour = the entity this repository manages
    //   Long = the type of the primary key (id)
    
    // Spring generates these automatically — you don't write any SQL:
    // findAll(), findById(id), save(tour), deleteById(id), count(), etc.

    // Custom full-text search: searches name, description, from, to, and tour log comments
    @Query("SELECT DISTINCT t FROM Tour t LEFT JOIN t.tourLogs l WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.fromLocation) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.toLocation) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.comment) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Tour> searchTours(String query);
}