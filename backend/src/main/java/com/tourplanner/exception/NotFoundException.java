package com.tourplanner.exception;

// thrown when we look for something by id and it just is not there.
// the handler turns this into a 404 response for the frontend.
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
