package com.trackingpath.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class GeoUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GeoUtils() {
    }

    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static String lineStringGeoJson(List<double[]> points) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("type", "Feature");
        ObjectNode geometry = root.putObject("geometry");
        geometry.put("type", "LineString");

        ArrayNode coords = geometry.putArray("coordinates");
        for (double[] p : points) {
            ArrayNode c = coords.addArray();
            c.add(p[1]);
            c.add(p[0]);
        }

        root.putObject("properties");
        return root.toString();
    }
}
