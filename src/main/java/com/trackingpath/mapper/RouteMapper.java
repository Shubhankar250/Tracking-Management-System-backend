package com.trackingpath.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.entities.Routes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBReader;
import java.util.*;

public class RouteMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final WKBReader wkbReader = new WKBReader();

    /* ---------------- ENTITY → MAP ---------------- */
    

    /* ---------------- WKB HEX → GEOJSON MAP ---------------- */
    private static Map<String, Object> convertWkbToGeoJson(String wkbHex) {
        if (wkbHex == null || wkbHex.isEmpty()) return null;

        try {
            // read hex → geometry
            byte[] wkb = hexToBytes(wkbHex);
            Geometry geom = wkbReader.read(wkb);

            // return as GeoJSON map
            Map<String, Object> geoJson = new HashMap<>();
            geoJson.put("type", geom.getGeometryType());
            geoJson.put("coordinates", convertCoordinates(geom));

            return geoJson;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Invalid WKB geometry: " + wkbHex);
            return null;
        }
    }

    /* ---------------- HEX → BYTE[] ---------------- */
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) 
                ((Character.digit(hex.charAt(i), 16) << 4)
                + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    /* ---------------- COORDINATES ---------------- */
    private static Object convertCoordinates(Geometry geometry) {
        switch (geometry.getGeometryType()) {
            case "Point":
                return List.of(
                        geometry.getCoordinate().x,
                        geometry.getCoordinate().y
                );

            case "LineString":
                List<List<Double>> line = new ArrayList<>();
                for (var c : geometry.getCoordinates()) {
                    line.add(List.of(c.x, c.y));
                }
                return line;

            case "Polygon":
                List<List<List<Double>>> poly = new ArrayList<>();
                List<List<Double>> ring = new ArrayList<>();

                for (var c : geometry.getCoordinates()) {
                    ring.add(List.of(c.x, c.y));
                }

                poly.add(ring);
                return poly;

            default:
                return null;
        }
    }
}
