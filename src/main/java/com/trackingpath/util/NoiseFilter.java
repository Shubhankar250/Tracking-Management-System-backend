package com.trackingpath.util;

import java.util.ArrayList;
import java.util.List;

import com.trackingpath.dtos.GeoUtil;
import com.trackingpath.dtos.GpsPoint;

public class NoiseFilter {

    private final double spikeDistanceMeters;

    public NoiseFilter(double spikeDistanceMeters) {
        this.spikeDistanceMeters = spikeDistanceMeters;
    }

    public List<GpsPoint> clean(List<GpsPoint> points) {
        if (points.size() < 3) {
            return points;
        }

        List<GpsPoint> result = new ArrayList<>();
        result.add(points.get(0));

        for (int i = 1; i < points.size() - 1; i++) {
            GpsPoint prev = points.get(i - 1);
            GpsPoint curr = points.get(i);
            GpsPoint next = points.get(i + 1);

            double d1 = GeoUtil.distanceMeters(prev, curr);
            double d2 = GeoUtil.distanceMeters(curr, next);

            if (d1 > spikeDistanceMeters && d2 > spikeDistanceMeters) {
                continue;
            }

            result.add(curr);
        }

        result.add(points.get(points.size() - 1));
        return result;
    }
}
