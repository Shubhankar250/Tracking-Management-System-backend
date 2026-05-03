package com.trackingpath.services;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.trackingpath.dtos.GpsPointDto;
import com.trackingpath.dtos.RouteImportResponse;
import com.trackingpath.dtos.StopDto;
import com.trackingpath.util.CsvUtils;
import com.trackingpath.util.GeoUtils;

@Service
public class GpsHistoryImportService {

    private final StopDetectionService stopDetectionService;

    public GpsHistoryImportService(StopDetectionService stopDetectionService) {
        this.stopDetectionService = stopDetectionService;
    }

    public RouteImportResponse importGpsCsv(MultipartFile file, int idleMinutes, double stopRadiusMeters) throws IOException {
        List<GpsPointDto> points = CsvUtils.parseGpsCsv(file.getInputStream());
        return buildResponse(points, idleMinutes, stopRadiusMeters);
    }

    public RouteImportResponse importGpsText(String csvText, int idleMinutes, double stopRadiusMeters) throws IOException {
        List<GpsPointDto> points = CsvUtils.parseGpsCsv(new java.io.ByteArrayInputStream(csvText.getBytes()));
        return buildResponse(points, idleMinutes, stopRadiusMeters);
    }

    private RouteImportResponse buildResponse(List<GpsPointDto> points, int idleMinutes, double stopRadiusMeters) {
        RouteImportResponse response = new RouteImportResponse();
        response.setSourceType("GPS_HISTORY");
        response.setRawGpsPoints(points);

        List<double[]> coords = new ArrayList<>();
        for (GpsPointDto p : points) {
            coords.add(new double[]{p.getLatitude(), p.getLongitude()});
        }

        List<StopDto> stops = stopDetectionService.detectStops(points, idleMinutes, stopRadiusMeters);

        response.setRouteGeoJson(GeoUtils.lineStringGeoJson(coords));
        response.setStops(stops);
        response.setMessage("GPS history imported successfully");
        return response;
    }
}
