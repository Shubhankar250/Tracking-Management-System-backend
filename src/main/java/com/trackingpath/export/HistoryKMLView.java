package com.trackingpath.export;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

import com.trackingpath.dtos.EventDataDTO;
import com.trackingpath.dtos.HistoryDataPlaybackDTO;

public class HistoryKMLView {

    public static byte[] generate(HistoryDataPlaybackDTO data) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        List<EventDataDTO> route = data.getEventDataList();

        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
        writer.println("<Document>");
        writer.println("<Placemark>");
        writer.println("<LineString>");
        writer.println("<coordinates>");

        for (EventDataDTO point : route) {
            writer.print(point.getLongitude() + "," + point.getLatitude() + ",0 ");
        }

        writer.println("</coordinates>");
        writer.println("</LineString>");
        writer.println("</Placemark>");
        writer.println("</Document>");
        writer.println("</kml>");

        writer.flush();
        return out.toByteArray();
    }
}
