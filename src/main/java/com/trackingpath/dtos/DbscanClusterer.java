package com.trackingpath.dtos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DbscanClusterer {

    private final double epsMeters;
    private final int minPoints;

    public DbscanClusterer(double epsMeters, int minPoints) {
        this.epsMeters = epsMeters;
        this.minPoints = minPoints;
    }

    public DbscanResult cluster(List<GpsPoint> gpsPoints) {
        List<ClusterPoint> points = gpsPoints.stream()
                .map(ClusterPoint::new)
                .toList();

        int clusterId = 0;
        List<List<GpsPoint>> clusters = new ArrayList<>();
        List<GpsPoint> noise = new ArrayList<>();

        for (ClusterPoint point : points) {
            if (point.isVisited()) {
                continue;
            }

            point.setVisited(true);
            List<ClusterPoint> neighbors = getNeighbors(points, point);

            if (neighbors.size() < minPoints) {
                noise.add(point.getGpsPoint());
                continue;
            }

            List<GpsPoint> cluster = new ArrayList<>();
            expandCluster(points, point, neighbors, cluster, clusterId);
            clusters.add(cluster);
            clusterId++;
        }

        return new DbscanResult(clusters, noise);
    }

    private void expandCluster(List<ClusterPoint> allPoints,
                               ClusterPoint seed,
                               List<ClusterPoint> seedNeighbors,
                               List<GpsPoint> cluster,
                               int clusterId) {

        seed.setClusterId(clusterId);
        cluster.add(seed.getGpsPoint());

        Queue<ClusterPoint> queue = new LinkedList<>(seedNeighbors);

        while (!queue.isEmpty()) {
            ClusterPoint current = queue.poll();

            if (!current.isVisited()) {
                current.setVisited(true);
                List<ClusterPoint> currentNeighbors = getNeighbors(allPoints, current);

                if (currentNeighbors.size() >= minPoints) {
                    queue.addAll(currentNeighbors);
                }
            }

            if (current.getClusterId() == -1) {
                current.setClusterId(clusterId);
                cluster.add(current.getGpsPoint());
            }
        }
    }

    private List<ClusterPoint> getNeighbors(List<ClusterPoint> allPoints, ClusterPoint center) {
        List<ClusterPoint> neighbors = new ArrayList<>();

        for (ClusterPoint candidate : allPoints) {
            double distance = GeoUtil.haversineMeters(
                    center.getGpsPoint().getLatitude(),
                    center.getGpsPoint().getLongitude(),
                    candidate.getGpsPoint().getLatitude(),
                    candidate.getGpsPoint().getLongitude()
            );

            if (distance <= epsMeters) {
                neighbors.add(candidate);
            }
        }

        return neighbors;
    }

    public static class DbscanResult {
        private final List<List<GpsPoint>> clusters;
        private final List<GpsPoint> noise;

        public DbscanResult(List<List<GpsPoint>> clusters, List<GpsPoint> noise) {
            this.clusters = clusters;
            this.noise = noise;
        }

        public List<List<GpsPoint>> getClusters() {
            return clusters;
        }

        public List<GpsPoint> getNoise() {
            return noise;
        }
    }
}
