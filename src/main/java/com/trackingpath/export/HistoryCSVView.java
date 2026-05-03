package com.trackingpath.export;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import com.opencsv.CSVWriter;
import com.trackingpath.dtos.EventDataDTO;
import com.trackingpath.dtos.HistoryDataPlaybackDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class HistoryCSVView {

	public static byte[] generate(HistoryDataPlaybackDTO data) throws Exception {

	    ByteArrayOutputStream out = new ByteArrayOutputStream();

	    // 🔥 Add UTF-8 BOM for Excel
	    out.write(0xEF);
	    out.write(0xBB);
	    out.write(0xBF);

	    CSVWriter writer = new CSVWriter(
	            new PrintWriter(out, true, java.nio.charset.StandardCharsets.UTF_8),
	            ',',                                // comma separator
	            CSVWriter.NO_QUOTE_CHARACTER,       // no quotes
	            CSVWriter.DEFAULT_ESCAPE_CHARACTER,
	            CSVWriter.DEFAULT_LINE_END
	    );

	    writer.writeNext(new String[]{
	            "Time","Latitude","Longitude","Altitude",
	            "Course","Speed","Ignition","Distance"
	    });

ObjectMapper mapper = new ObjectMapper();
	    for (EventDataDTO event : data.getEventDataList()) {

	        boolean ignition = false;

	        try {
	            String attr = event.getAttributes(); // JSON string
	            if (attr != null && !attr.isEmpty()) {
	                JsonNode node = mapper.readTree(attr);
	                ignition = node.has("ignition") && node.get("ignition").asBoolean();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        writer.writeNext(new String[]{
	                event.getDeviceTime(),
	                String.valueOf(event.getLatitude()),
	                String.valueOf(event.getLongitude()),
	                String.valueOf(event.getAltitude()),
	                String.valueOf(event.getCourse()),
	                String.valueOf(event.getSpeed()),
	                ignition ? "ON" : "OFF",
	                String.valueOf(event.getDistance())
	        });
	    }

	    writer.close();
	    return out.toByteArray();
	}

}