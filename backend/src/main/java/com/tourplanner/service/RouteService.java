package com.tourplanner.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// this works out real routes by asking an outside map service (openrouteservice).
// it happens in two steps. first it turns a place name like "vienna" into map coordinates
// (numbers that pin a spot on earth). then it feeds two of those points to the map service
// and gets back the distance, the travel time, and the line to draw on the map.
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
        public final String estimatedTime;
        public final String geometryJson; // the route as a json array of [lon,lat] pairs

        public RouteResult(double distanceKm, String estimatedTime, String geometryJson) {
            this.distanceKm = distanceKm;
            this.estimatedTime = estimatedTime;
            this.geometryJson = geometryJson;
        }
    }

    // calls the directions api and returns real distance and time between two locations
    // returns null if the route cannot be calculated
    public RouteResult getRoute(String fromLocation, String toLocation, String transportType) {
        try {
            double[] from = geocode(fromLocation);
            double[] to   = geocode(toLocation);

            if (from == null || to == null) {
                logger.warn("Could not geocode locations: {} -> {}", fromLocation, toLocation);
                return null;
            }

            String profile = switch (transportType) {
                case "Bike" -> "cycling-regular";
                case "Run"  -> "foot-running";
                case "Hike" -> "foot-hiking";
                case "Walk" -> "foot-walking";
                default     -> "driving-car";
            };

            // we ask for geojson geometry so we get plain coordinates, not encoded polyline
            String body = String.format(
                "{\"coordinates\":[[%f,%f],[%f,%f]],\"geometry_simplify\":\"true\"}",
                from[0], from[1], to[0], to[1]
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            String url = "https://api.openrouteservice.org/v2/directions/" + profile + "/geojson";

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            JsonNode root    = objectMapper.readTree(response.getBody());
            JsonNode feature = root.path("features").get(0);
            JsonNode props   = feature.path("properties").path("summary");

            double distanceMetres  = props.path("distance").asDouble();
            double durationSeconds = props.path("duration").asDouble();

            double distanceKm = Math.round(distanceMetres / 100.0) / 10.0;

            long totalMinutes  = (long) (durationSeconds / 60);
            String estimatedTime = String.format("%02d:%02d",
                    totalMinutes / 60, totalMinutes % 60);

            // the geometry is a list of [lon,lat] coordinate pairs
            // we store it as a json string so we can save it in the database as text
            JsonNode geometryCoords = feature.path("geometry").path("coordinates");
            String geometryJson = objectMapper.writeValueAsString(geometryCoords);

            logger.info("Route from '{}' to '{}': {} km, {}, {} points",
                    fromLocation, toLocation, distanceKm, estimatedTime,
                    geometryCoords.size());

            return new RouteResult(distanceKm, estimatedTime, geometryJson);

        } catch (Exception e) {
            logger.error("Failed to get route from {} to {}", fromLocation, toLocation, e);
            return null;
        }
    }
}