package com.trackingpath.services;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import com.trackingpath.dtos.RouteImportResponse;
import com.trackingpath.dtos.StopDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KmlImportService {

    public RouteImportResponse parseKml(String kmlText) {

        RouteImportResponse response = new RouteImportResponse();
        response.setSourceType("KML");

        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(kmlText)));

            NodeList placemarks = doc.getElementsByTagName("Placemark");

            List<double[]> routePoints = new ArrayList<>();
            List<StopDto> stops = new ArrayList<>();

            int seq = 1;
            String geometryType = null; // Track what we found

            for (int i = 0; i < placemarks.getLength(); i++) {

                Element placemark = (Element) placemarks.item(i);
                String name = getChildText(placemark, "name");

                // ================= LINESTRING =================
                NodeList lineStrings = placemark.getElementsByTagName("LineString");
                if (lineStrings.getLength() > 0) {
                    Element line = (Element) lineStrings.item(0);
                    String coordText = getChildText(line, "coordinates");

                    routePoints.addAll(parseCoordinates(coordText));
                    geometryType = "LineString";
                }

                // ================= POLYGON =================
                NodeList polygons = placemark.getElementsByTagName("Polygon");
                if (polygons.getLength() > 0) {

                    Element polygon = (Element) polygons.item(0);
                    NodeList rings = polygon.getElementsByTagName("LinearRing");

                    if (rings.getLength() > 0) {
                        Element ring = (Element) rings.item(0);
                        String coordText = getChildText(ring, "coordinates");

                        routePoints.addAll(parseCoordinates(coordText));
                        geometryType = "Polygon";
                    }
                }

                // ================= POINT (STOPS) =================
                NodeList points = placemark.getElementsByTagName("Point");
                if (points.getLength() > 0) {

                    Element point = (Element) points.item(0);
                    String coordText = getChildText(point, "coordinates");

                    List<double[]> coords = parseCoordinates(coordText);

                    if (!coords.isEmpty()) {
                        double[] p = coords.get(0);

                        StopDto stop = new StopDto();
                        stop.setSequenceNo(seq++);
                        stop.setStopName(name != null && !name.isBlank() ? name : "Stop " + (seq - 1));
                        stop.setLatitude(p[0]);
                        stop.setLongitude(p[1]);
                        stop.setStopType("PICKUP");
                        stop.setGeofenceRadius(50);
                        stop.setAutoDetected(false);
                        stop.setApproved(true);
                        stop.setPassengerCount(0);

                        stops.add(stop);
                    }
                }
            }

            // ================= GEOJSON BUILD =================
            if (!routePoints.isEmpty()) {

                if ("Polygon".equals(geometryType)) {
                    response.setRouteGeoJson(buildPolygonGeoJson(routePoints));
                } else {
                    response.setRouteGeoJson(buildLineStringGeoJson(routePoints));
                }

            } else {
                response.setRouteGeoJson("");
            }

            response.setStops(stops);
            response.setMessage("KML parsed successfully");

            return response;

        } catch (Exception e) {
            log.error("Error parsing KML", e);
            throw new RuntimeException("Invalid KML format", e);
        }
    }

    // ================= HELPER METHODS =================

    private String getChildText(Element parent, String tagName) {
        NodeList nl = parent.getElementsByTagName(tagName);
        if (nl.getLength() == 0) return null;
        return nl.item(0).getTextContent();
    }

    private List<double[]> parseCoordinates(String coordText) {

        List<double[]> points = new ArrayList<>();

        if (coordText == null || coordText.isBlank()) return points;

        String[] lines = coordText.trim().split("\\s+");

        for (String line : lines) {
            String[] arr = line.split(",");

            if (arr.length >= 2) {
                double lng = Double.parseDouble(arr[0].trim());
                double lat = Double.parseDouble(arr[1].trim());

                points.add(new double[]{lat, lng}); // store as lat, lng
            }
        }

        return points;
    }

    // ================= GEOJSON BUILDERS =================

    private String buildLineStringGeoJson(List<double[]> points) {

        StringBuilder sb = new StringBuilder();

        sb.append("{\"type\":\"Feature\",\"geometry\":{");
        sb.append("\"type\":\"LineString\",\"coordinates\":[");

        for (int i = 0; i < points.size(); i++) {
            double[] p = points.get(i);

            sb.append("[").append(p[1]).append(",").append(p[0]).append("]");

            if (i < points.size() - 1) sb.append(",");
        }

        sb.append("]},\"properties\":{}}");

        return sb.toString();
    }

    private String buildPolygonGeoJson(List<double[]> points) {

        StringBuilder sb = new StringBuilder();

        sb.append("{\"type\":\"Feature\",\"geometry\":{");
        sb.append("\"type\":\"Polygon\",\"coordinates\":[[");

        for (int i = 0; i < points.size(); i++) {
            double[] p = points.get(i);

            sb.append("[").append(p[1]).append(",").append(p[0]).append("]");

            if (i < points.size() - 1) sb.append(",");
        }

        sb.append("]]},\"properties\":{}}");

        return sb.toString();
    }
}