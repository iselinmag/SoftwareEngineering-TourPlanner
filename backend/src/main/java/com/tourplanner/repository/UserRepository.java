package com.tourplanner.repository;

import com.tourplanner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// this is the drawer where users are stored.
// spring gives us the everyday actions for free (save, find by id, delete and so on),
// so we only add the one extra lookup we need below.
public interface UserRepository extends JpaRepository<User, Long> {
    // find one user by their username, used when someone logs in
    Optional<User> findByUsername(String username);
}