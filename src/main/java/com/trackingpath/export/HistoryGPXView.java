package com.trackingpath.export;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import com.trackingpath.dtos.EventDataDTO;
import com.trackingpath.dtos.HistoryDataPlaybackDTO;

public class HistoryGPXView {

    public static byte[] generate(HistoryDataPlaybackDTO data) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<gpx version=\"1.1\">");
        writer.println("<trk><trkseg>");

        for (EventDataDTO point : data.getEventDataList()) {
            writer.print("<trkpt lat=\"" + point.getLatitude() + "\" lon=\"" + point.getLongitude() + "\">");
            writer.print("<time>" + point.getDeviceTime() + "</time>");
            writer.println("</trkpt>");
        }

        writer.println("</trkseg></trk></gpx>");
        writer.flush();

        return out.toByteArray();
    }
}
