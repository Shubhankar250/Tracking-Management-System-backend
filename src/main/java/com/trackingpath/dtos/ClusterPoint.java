package com.trackingpath.dtos;

public class ClusterPoint {

    private final GpsPoint gpsPoint;
    private boolean visited;
    private int clusterId = -1;

    public ClusterPoint(GpsPoint gpsPoint) {
        this.gpsPoint = gpsPoint;
    }

    public GpsPoint getGpsPoint() {
        return gpsPoint;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }
}
