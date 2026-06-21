package com.tourplanner.exception;

// thrown when someone is logged in but tries to touch something that is not theirs,
// like editing another person's tour or log.
// the handler turns this into a 403 response for the frontend.
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
