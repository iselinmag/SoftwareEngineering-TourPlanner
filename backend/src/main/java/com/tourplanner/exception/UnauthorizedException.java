package com.tourplanner.exception;

// thrown when the login details do not check out, like a wrong username or password.
// the handler turns this into a 401 response for the frontend.
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
