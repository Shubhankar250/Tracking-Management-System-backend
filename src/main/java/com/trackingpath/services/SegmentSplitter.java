package com.trackingpath.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.trackingpath.dtos.GeoUtil;
import com.trackingpath.dtos.GpsPoint;

public class SegmentSplitter {

    private final long timeGapSeconds;
    private final double jumpDistanceMeters;

    public SegmentSplitter(long timeGapSeconds, double jumpDistanceMeters) {
        this.timeGapSeconds = timeGapSeconds;
        this.jumpDistanceMeters = jumpDistanceMeters;
    }

    public List<List<GpsPoint>> split(List<GpsPoint> clusterPoints) {
        List<GpsPoint> ordered = clusterPoints.stream()
                .sorted(Comparator.comparing(GpsPoint::getDeviceTime))
                .toList();

        List<List<GpsPoint>> segments = new ArrayList<>();
        List<GpsPoint> current = new ArrayList<>();

        for (int i = 0; i < ordered.size(); i++) {
            GpsPoint point = ordered.get(i);

            if (i == 0) {
                current.add(point);
                continue;
            }

            GpsPoint prev = ordered.get(i - 1);

            long gap = GeoUtil.timeGapSeconds(prev, point);
            double jump = GeoUtil.distanceMeters(prev, point);

            if (gap > timeGapSeconds || jump > jumpDistanceMeters) {
                if (current.size() >= 2) {
                    segments.add(current);
                }
                current = new ArrayList<>();
            }

            current.add(point);
        }

        if (current.size() >= 2) {
            segments.add(current);
        }

        return segments;
    }
}
