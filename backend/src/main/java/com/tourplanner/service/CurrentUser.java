package com.tourplanner.service;

import com.tourplanner.entity.User;
import com.tourplanner.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// this is a small helper that answers one question: who is making this request right now?
// the ticket checker already noted the username earlier, this just fetches the full user
// record for it so the rest of the code can say "is this thing yours?".
@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // who is logged in for this request?
    public User get() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Not logged in"));
    }
}