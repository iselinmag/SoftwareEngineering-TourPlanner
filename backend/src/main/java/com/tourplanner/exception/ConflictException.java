package com.tourplanner.exception;

// thrown when something clashes with what is already there, like picking a username
// that someone else has already taken.
// the handler turns this into a 409 response for the frontend.
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
