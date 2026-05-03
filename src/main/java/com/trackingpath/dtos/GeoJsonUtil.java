package com.trackingpath.dtos;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

public class GeoJsonUtil {

    private static final GeoJsonWriter WRITER = new GeoJsonWriter();

    private GeoJsonUtil() {
    }

    public static String toGeoJson(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        return WRITER.write(geometry);
    }
}
