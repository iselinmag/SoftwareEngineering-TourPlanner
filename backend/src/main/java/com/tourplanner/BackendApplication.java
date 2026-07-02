package com.tourplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// this is the on switch for the whole backend.
// when you run the app, java starts here and spring boot wakes up all the other
// pieces for us (the database, the web routes, the security checks and so on).
@SpringBootApplication
public class BackendApplication {
    // the very first thing that runs, it just tells spring to start everything up
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}