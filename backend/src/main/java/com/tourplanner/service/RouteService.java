package com.tourplanner.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// First transform to and from location name to coordinates
        // Then use coordinates to call API to get distance and etc.
        
@Service
public class RouteService {

    private static final Logger logger = LogManager.getLogger(RouteService.class);

    // reads the api key from application.properties automatically
    @Value("${openrouteservice.api.key}")
    private String apiKey;

    // resttemplate is spring's built in http client for making requests to external apis
    private final RestTemplate restTemplate = new RestTemplate();

    // jackson objectmapper parses json responses into java objects
    private final ObjectMapper objectMapper = new ObjectMapper();

    // geocodes a place name into [longitude, latitude] coordinates
    // returns null if the location cannot be found
    public double[] geocode(String placeName) {
        try {
            String url = "https://api.openrouteservice.org/geocode/search?text="
                    + placeName.replace(" ", "%20")
                    + "&size=1";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            // the response is a geojson object we search it to find coordinates
            // structure: features[0].geometry.coordinates = [longitude, latitude]
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode coords = root.path("features").get(0).path("geometry").path("coordinates");

            double longitude = coords.get(0).asDouble();
            double latitude  = coords.get(1).asDouble();

            logger.info("Geocoded '{}' to [{}, {}]", placeName, longitude, latitude);
            return new double[]{longitude, latitude};

        } catch (Exception e) {
            logger.error("Failed to geocode location: {}", placeName, e);
            return null;
        }
    }

    // holds the result of a route calculation
    public static class RouteResult {
        public final double distanceKm;
        public final String estimatedTime; // formatted as "HH:mm"

        public RouteResult(double distanceKm, String estimatedTime) {
            this.distanceKm = distanceKm;
            this.estimatedTime = estimatedTime;
        }
    }

    // calls the directions api and returns real distance and time between two locations
    // returns null if the route cannot be calculated
    public RouteResult getRoute(String fromLocation, String toLocation, String transportType) {
        try {
            // step 1: convert text locations to coordinates
            double[] from = geocode(fromLocation);
            double[] to = geocode(toLocation);

            if (from == null || to == null) {
                logger.warn("Could not geocode locations: {} -> {}", fromLocation, toLocation);
                return null;
            }

            // step 2: pick the right ors profile based on transport type
            // ors uses different routing profiles for different transport modes
            String profile = switch (transportType) {
                case "Bike" -> "cycling-regular";
                case "Run"  -> "foot-running";
                case "Hike" -> "foot-hiking";
                case "Walk" -> "foot-walking";
                default     -> "driving-car";  // Car, Boat
            };

            // step 3: build the request body as json
            // ors expects coordinates as [[lon,lat],[lon,lat]]
            String body = String.format(
                "{\"coordinates\":[[%f,%f],[%f,%f]]}",
                from[0], from[1], to[0], to[1]
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            String url = "https://api.openrouteservice.org/v2/directions/" + profile;

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            // step 4: parse the response
            // structure: routes[0].summary.distance (metres) + duration (seconds)
            JsonNode root     = objectMapper.readTree(response.getBody());
            JsonNode summary  = root.path("routes").get(0).path("summary");

            double distanceMetres = summary.path("distance").asDouble();
            double durationSeconds = summary.path("duration").asDouble();

            // convert metres to km, rounded to 1 decimal
            double distanceKm = Math.round(distanceMetres / 100.0) / 10.0;

            // convert seconds to "HH:mm" format
            long totalMinutes = (long) (durationSeconds / 60);
            String estimatedTime = String.format("%02d:%02d",
                    totalMinutes / 60, totalMinutes % 60);

            logger.info("Route from '{}' to '{}': {} km, {}",
                    fromLocation, toLocation, distanceKm, estimatedTime);

            return new RouteResult(distanceKm, estimatedTime);

        } catch (Exception e) {
            logger.error("Failed to get route from {} to {}", fromLocation, toLocation, e);
            return null;
        }
    }
}