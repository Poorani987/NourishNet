package com.nourishnet.util;

/**
 * Geographic utility for Haversine straight-line distance calculations
 * and transport policy evaluation.
 * Note: Haversine distance is strictly for the 15-km transport policy rule;
 * road routes are calculated using Dijkstra's shortest-path algorithm.
 */
public class GeoUtils {
    public static final double POLICY_THRESHOLD_KM = 15.0;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public static final String POLICY_LOCAL = "LOCAL_TRANSPORT_ELIGIBLE (<= 15 km): Transport may be arranged by donor or suitable volunteer driver.";
    public static final String POLICY_EXTENDED = "EXTENDED_DISTANCE (> 15 km): Recipient is responsible for arranging pickup or coordinating an appropriate volunteer.";
    public static final String POLICY_COORDINATES_MISSING = "COORDINATES_MISSING: Coordinates not fully provided. Exact 15-km policy check pending GPS location details.";

    /**
     * Calculates the great-circle distance between two GPS coordinates using the Haversine formula.
     *
     * @return Distance in kilometers, rounded to 2 decimal places. Returns -1.0 if coordinates are null or invalid.
     */
    public static double calculateHaversineDistanceKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return -1.0;
        }

        // Validate coordinate bounds
        if (lat1 < -90 || lat1 > 90 || lat2 < -90 || lat2 > 90 ||
            lon1 < -180 || lon1 > 180 || lon2 < -180 || lon2 > 180) {
            return -1.0;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        return Math.round(distance * 100.0) / 100.0;
    }

    /**
     * Evaluates the transport policy for the given distance in km.
     */
    public static String evaluateTransportPolicy(double distanceKm) {
        if (distanceKm < 0) {
            return POLICY_COORDINATES_MISSING;
        }
        if (distanceKm <= POLICY_THRESHOLD_KM) {
            return POLICY_LOCAL;
        }
        return POLICY_EXTENDED;
    }

    /**
     * Evaluates transport policy directly from coordinates.
     */
    public static String evaluateTransportPolicy(Double lat1, Double lon1, Double lat2, Double lon2) {
        double dist = calculateHaversineDistanceKm(lat1, lon1, lat2, lon2);
        return evaluateTransportPolicy(dist);
    }
}
