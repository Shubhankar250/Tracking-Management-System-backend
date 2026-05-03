package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LatLng {
    private double lat;
    private double lng;

    @Override
    public String toString() {
        return "[" + lat + ", " + lng + "]";
    }
}