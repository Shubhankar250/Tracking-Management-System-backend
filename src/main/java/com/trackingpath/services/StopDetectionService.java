package com.trackingpath.services;


import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.trackingpath.dtos.GpsPointDto;
import com.trackingpath.dtos.StopDto;
import com.trackingpath.util.GeoUtils;

@Service
public class StopDetectionService {

    public List<StopDto> detectStops(List<GpsPointDto> points, int minIdleMinutes, double maxClusterRadiusMeters) {
        List<StopDto> stops = new ArrayList<>();
        if (points == null || points.size() < 2) {
            return stops;
        }

        points.sort(Comparator.comparing(GpsPointDto::getFixTime));

        int sequence = 1;
        int i = 0;
        while (i < points.size()) {
            GpsPointDto anchor = points.get(i);

            List<GpsPointDto> cluster = new ArrayList<>();
            cluster.add(anchor);

            int j = i + 1;
            while (j < points.size()) {
                GpsPointDto candidate = points.get(j);

                double distance = GeoUtils.distanceMeters(
                        anchor.getLatitude(), anchor.getLongitude(),
                        candidate.getLatitude(), candidate.getLongitude()
                );

                if (distance <= maxClusterRadiusMeters) {
                    cluster.add(candidate);
                    j++;
                } else {
                    break;
                }
            }

            if (cluster.size() >= 2) {
                Duration duration = Duration.between(cluster.get(0).getFixTime(), cluster.get(cluster.size() - 1).getFixTime());
                if (duration.toMinutes() >= minIdleMinutes) {
                    double avgLat = cluster.stream().mapToDouble(GpsPointDto::getLatitude).average().orElse(anchor.getLatitude());
                    double avgLng = cluster.stream().mapToDouble(GpsPointDto::getLongitude).average().orElse(anchor.getLongitude());

                    StopDto stop = new StopDto();
                    stop.setSequenceNo(sequence++);
                    stop.setStopName("Auto Stop " + (sequence - 1));
                    stop.setLatitude(avgLat);
                    stop.setLongitude(avgLng);
                    stop.setStopType("PICKUP");
                    stop.setGeofenceRadius((int) maxClusterRadiusMeters);
                    stop.setAutoDetected(true);
                    stop.setApproved(false);
                    stop.setPassengerCount(0);

                    stops.add(stop);
                    i = j;
                    continue;
                }
            }

            i++;
        }

        return mergeNearbyStops(stops, maxClusterRadiusMeters);
    }

    private List<StopDto> mergeNearbyStops(List<StopDto> stops, double mergeRadiusMeters) {
        List<StopDto> result = new ArrayList<>();

        for (StopDto stop : stops) {
            boolean merged = false;
            for (StopDto existing : result) {
                double d = GeoUtils.distanceMeters(
                        existing.getLatitude(), existing.getLongitude(),
                        stop.getLatitude(), stop.getLongitude()
                );
                if (d <= mergeRadiusMeters) {
                    existing.setLatitude((existing.getLatitude() + stop.getLatitude()) / 2.0);
                    existing.setLongitude((existing.getLongitude() + stop.getLongitude()) / 2.0);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                result.add(stop);
            }
        }

        int seq = 1;
        for (StopDto stop : result) {
            stop.setSequenceNo(seq++);
            stop.setStopName("Auto Stop " + stop.getSequenceNo());
        }

        return result;
    }
}
