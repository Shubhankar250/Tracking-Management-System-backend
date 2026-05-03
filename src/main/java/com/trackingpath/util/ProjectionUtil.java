package com.trackingpath.util;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class ProjectionUtil {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public static MathTransform buildWgs84ToUtmTransform(double longitude, double latitude) throws Exception {
        int zone = (int) Math.floor((longitude + 180.0) / 6.0) + 1;

        String epsg = latitude >= 0
                ? String.format("EPSG:%d", 32600 + zone)
                : String.format("EPSG:%d", 32700 + zone);

        CoordinateReferenceSystem src = CRS.decode("EPSG:4326", true);
        CoordinateReferenceSystem tgt = CRS.decode(epsg, true);

        return CRS.findMathTransform(src, tgt, true);
    }

    public static Coordinate toUtm(double longitude, double latitude, MathTransform transform) throws Exception {
        Point wgsPoint = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        Point utmPoint = (Point) JTS.transform(wgsPoint, transform);
        return utmPoint.getCoordinate();
    }
}