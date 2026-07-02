package com.tourplanner.service;

import com.tourplanner.entity.User;
import com.tourplanner.repository.UserRepository;
import com.tourplanner.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// this handles signing up and logging in.
// it is the reception desk: it takes a username and password, checks them, scrambles the
// password so the real one is never stored, and hands back a login ticket when all is well.
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // sign up a brand new user
    public String register(String username, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already taken");
        }
        User user = new User();
        user.setUsername(username);
        // scramble the password before saving, never store the real one
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        // hand them a ticket straight away so they are logged in
        return jwtService.makeToken(username);
    }

    // log an existing user in
    public String login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Wrong username or password"));
        // compare the typed password against the stored scramble
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Wrong username or password");
        }
        return jwtService.makeToken(username);
    }
}