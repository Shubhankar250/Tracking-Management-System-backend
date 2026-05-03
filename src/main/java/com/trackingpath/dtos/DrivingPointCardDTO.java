package com.trackingpath.dtos;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DrivingPointCardDTO {

    private String seg_running_time;
    private String movment_start_time;
    private String movment_end_time;

    private double seg_max_speed;
    private double seg_avg_speed;
    private double seg_distance;

    private String seg_address;

    private List<LatLng> polylinePoints = new ArrayList<>();

    private String polyline = "";
}
