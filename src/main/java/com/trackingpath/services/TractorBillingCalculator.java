package com.trackingpath.services;

import org.springframework.stereotype.Service;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import com.trackingpath.dtos.BillingFieldResult;
import com.trackingpath.dtos.BillingSummary;
import com.trackingpath.dtos.DbscanClusterer;
import com.trackingpath.dtos.GeoJsonUtil;
import com.trackingpath.dtos.GpsPoint;
import com.trackingpath.util.NoiseFilter;
import com.trackingpath.util.ProjectionUtil;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TractorBillingCalculator {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final double minWorkingSpeed = 1.0;
    private final double maxWorkingSpeed = 8.0;

    private final DbscanClusterer dbscanClusterer;
    private final NoiseFilter noiseFilter;
    private final SegmentSplitter segmentSplitter;
    private final int minimumBillableClusterPoints;

    public TractorBillingCalculator() {
        this.dbscanClusterer = new DbscanClusterer(15.0, 8);
        this.noiseFilter = new NoiseFilter(50.0);
        this.segmentSplitter = new SegmentSplitter(15 * 60, 80.0);
        this.minimumBillableClusterPoints = 50;
    }

    public BillingSummary calculate(List<GpsPoint> rawPoints, double tractorWidthMeters) throws Exception {
        if (rawPoints == null || rawPoints.isEmpty()) {
            throw new IllegalArgumentException("GPS points are empty");
        }
        if (tractorWidthMeters <= 0) {
            throw new IllegalArgumentException("tractorWidthMeters must be greater than 0");
        }

        List<GpsPoint> sortedRaw = rawPoints.stream()
                .sorted(Comparator.comparing(GpsPoint::getDeviceTime))
                .toList();

        List<GpsPoint> workingPoints = sortedRaw.stream()
                .filter(p -> p.getSpeed() >= minWorkingSpeed && p.getSpeed() <= maxWorkingSpeed)
                .toList();

        List<GpsPoint> cleanWorking = noiseFilter.clean(workingPoints);

        DbscanClusterer.DbscanResult clusterResult = dbscanClusterer.cluster(cleanWorking);

        BillingSummary summary = new BillingSummary();
        summary.setDeviceId(sortedRaw.get(0).getDeviceId());
        summary.setTractorWidthMeters(tractorWidthMeters);
        summary.setRawPoints(sortedRaw.size());
        summary.setWorkingPoints(cleanWorking.size());
        summary.setNoisePoints(clusterResult.getNoise().size());

        int fieldNo = 1;
        double totalWorkedSqm = 0.0;

        for (int clusterId = 0; clusterId < clusterResult.getClusters().size(); clusterId++) {
            List<GpsPoint> cluster = clusterResult.getClusters().get(clusterId);

            if (cluster.size() < minimumBillableClusterPoints) {
                continue;
            }

            BillingFieldResult fieldResult = calculateField(clusterId, fieldNo, cluster, tractorWidthMeters);
            if (fieldResult.getWorkedAreaSqm() <= 0) {
                continue;
            }

            summary.getFields().add(fieldResult);
            totalWorkedSqm += fieldResult.getWorkedAreaSqm();
            fieldNo++;
        }

        summary.setTotalFields(fieldNo - 1);
        summary.setTotalWorkedAreaSqm(round(totalWorkedSqm));
        summary.setTotalWorkedAreaHectare(round(totalWorkedSqm / 10000.0));
        summary.setTotalWorkedAreaAcre(round(totalWorkedSqm / 4046.8564224));

        return summary;
    }

    private BillingFieldResult calculateField(int clusterId,
                                              int fieldNo,
                                              List<GpsPoint> clusterPoints,
                                              double tractorWidthMeters) throws Exception {

        List<GpsPoint> ordered = clusterPoints.stream()
                .sorted(Comparator.comparing(GpsPoint::getDeviceTime))
                .toList();

        List<List<GpsPoint>> segments = segmentSplitter.split(ordered);
        if (segments.isEmpty()) {
            return emptyField(clusterId, fieldNo, ordered);
        }

        org.geotools.api.referencing.operation.MathTransform transform = ProjectionUtil.buildWgs84ToUtmTransform(
                ordered.get(0).getLongitude(),
                ordered.get(0).getLatitude()
        );

        List<Geometry> bufferedSegments = new ArrayList<>();
        List<Coordinate> clusterUtmCoordinates = new ArrayList<>();

        for (GpsPoint point : ordered) {
            clusterUtmCoordinates.add(
                    ProjectionUtil.toUtm(point.getLongitude(), point.getLatitude(), transform)
            );
        }

        for (List<GpsPoint> segment : segments) {
            if (segment.size() < 2) {
                continue;
            }

            Coordinate[] utmCoords = new Coordinate[segment.size()];
            for (int i = 0; i < segment.size(); i++) {
                GpsPoint point = segment.get(i);
                utmCoords[i] = ProjectionUtil.toUtm(point.getLongitude(), point.getLatitude(), transform);
            }

            LineString lineString = GEOMETRY_FACTORY.createLineString(utmCoords);

            Geometry buffered = lineString.buffer(
                    tractorWidthMeters / 2.0,
                    8,
                    BufferParameters.CAP_FLAT
            );

            bufferedSegments.add(buffered);
        }

        if (bufferedSegments.isEmpty()) {
            return emptyField(clusterId, fieldNo, ordered);
        }

        Geometry workedGeometry = UnaryUnionOp.union(bufferedSegments);
        Geometry clusterBoundary = buildClusterBoundary(clusterUtmCoordinates);

        double workedAreaSqm = workedGeometry.getArea();
        double boundaryAreaSqm = clusterBoundary == null ? 0.0 : clusterBoundary.getArea();

        BillingFieldResult result = new BillingFieldResult();
        result.setFieldNo(fieldNo);
        result.setClusterId(clusterId);
        result.setTotalPoints(ordered.size());
        result.setSegmentCount(segments.size());
        
        String formattedStartTimeStr = ordered.get(0).getDeviceTime().format(formatter);
        result.setStartTime(formattedStartTimeStr);
        String formattedEndTimeStr = ordered.get(ordered.size() - 1).getDeviceTime().format(formatter);
        result.setEndTime(formattedEndTimeStr);
        
        result.setWorkedGeometry(workedGeometry);
        result.setClusterBoundaryGeometry(clusterBoundary);

        result.setWorkedAreaSqm(round(workedAreaSqm));
        result.setWorkedAreaHectare(round(workedAreaSqm / 10000.0));
        result.setWorkedAreaAcre(round(workedAreaSqm / 4046.8564224));

        result.setClusterBoundaryAreaSqm(round(boundaryAreaSqm));
        result.setClusterBoundaryAreaHectare(round(boundaryAreaSqm / 10000.0));
        result.setClusterBoundaryAreaAcre(round(boundaryAreaSqm / 4046.8564224));

        result.setWorkedGeoJson(GeoJsonUtil.toGeoJson(workedGeometry));
        result.setClusterBoundaryGeoJson(GeoJsonUtil.toGeoJson(clusterBoundary));

        return result;
    }

    private Geometry buildClusterBoundary(List<Coordinate> utmCoordinates) {
        if (utmCoordinates == null || utmCoordinates.size() < 4) {
            return null;
        }

        MultiPoint multiPoint = GEOMETRY_FACTORY.createMultiPointFromCoords(
                utmCoordinates.toArray(new Coordinate[0])
        );

        Geometry boundary = multiPoint.convexHull();

        if (boundary == null || boundary.isEmpty()) {
            return null;
        }

        return boundary;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private BillingFieldResult emptyField(int clusterId, int fieldNo, List<GpsPoint> ordered) {
        BillingFieldResult result = new BillingFieldResult();
        result.setFieldNo(fieldNo);
        result.setClusterId(clusterId);
        result.setTotalPoints(ordered.size());
        result.setSegmentCount(0);
        String formattedStartTimeStr = ordered.get(0).getDeviceTime().format(formatter);
        result.setStartTime(formattedStartTimeStr);
        String formattedEndTimeStr = ordered.get(ordered.size() - 1).getDeviceTime().format(formatter);
        result.setEndTime(formattedEndTimeStr);
        result.setWorkedAreaSqm(0);
        result.setWorkedAreaHectare(0);
        result.setWorkedAreaAcre(0);
        result.setClusterBoundaryAreaSqm(0);
        result.setClusterBoundaryAreaHectare(0);
        result.setClusterBoundaryAreaAcre(0);
        return result;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}