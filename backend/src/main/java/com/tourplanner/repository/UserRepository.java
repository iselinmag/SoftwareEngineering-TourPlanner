package com.tourplanner.repository;

import com.tourplanner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // lets us look someone up by their username at login time
    Optional<User> findByUsername(String username);
}