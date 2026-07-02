package com.tourplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.tourplanner.security.JwtAuthFilter;
import java.util.List;

// this is the security desk for the whole backend.
// it decides who is allowed through, which doors are open to everyone, and how the
// frontend is allowed to talk to us. think of it as the guard and the rulebook at the entrance.
@Configuration
public class SecurityConfig {

    // our own ticket checker, it reads the login token on each request
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // the main rulebook, it says which requests are let through and which need a valid login
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // turn on cors using the rules defined in the bean below
            .cors(cors -> {})
            // we turn csrf off because we use tokens, not browser sessions
            .csrf(csrf -> csrf.disable())
            // no server side session, every request carries its own ticket
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // let the browser's permission check (preflight) through without a token
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // the front desk is open to everyone
                .requestMatchers("/api/auth/**").permitAll()
                // tour images can be viewed by anyone, just like tours themselves
                .requestMatchers("/images/**").permitAll()
                // everything else needs a valid ticket
                .anyRequest().authenticated()
            )
            // check the ticket before the normal login check runs
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // tells the backend which frontend is allowed to call it, and how
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // the machine that scrambles passwords. bcrypt is the industry standard
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // spring uses this when checking a username and password at login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
