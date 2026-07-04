package com.tourplanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

// this is the shape of what someone sends when they sign up or log in.
// before this class existed, the auth endpoints took a loose map of text, which meant an
// empty username or password would sail straight through to the service. now the rules
// are written on the box itself: both fields must be filled in, and spring checks them
// at the door before any real work happens - the same way TourDTO and TourLogDTO do it.
@Data
@NoArgsConstructor
public class AuthRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 100, message = "Password must be between 4 and 100 characters")
    private String password;
}