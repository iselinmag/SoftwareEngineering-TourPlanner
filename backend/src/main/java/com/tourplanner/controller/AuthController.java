package com.tourplanner.controller;

import com.tourplanner.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// this is the front door for signing up and logging in.
// a controller is the part that listens for requests coming from the browser and answers them.
// it does no real work itself, it just takes the request and passes it to the auth service.
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/register  body: { "username": "...", "password": "..." }
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String token = authService.register(body.get("username"), body.get("password"));
        return ResponseEntity.ok(Map.of("token", token));
    }

    // POST /api/auth/login  body: { "username": "...", "password": "..." }
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String token = authService.login(body.get("username"), body.get("password"));
        return ResponseEntity.ok(Map.of("token", token));
    }
}