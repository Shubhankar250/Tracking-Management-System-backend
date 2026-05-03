package com.trackingpath.dtos;

import org.locationtech.jts.geom.Geometry;
import lombok.Data;

@Data
public class FieldResult {

    public int fieldNo;

    public double workedAreaSqm;
    public double fieldAreaSqm;
    public double coverage;

    public Geometry track;
    public Geometry worked;
    public Geometry boundary;

	public long startTime;

	public long endTime;
}