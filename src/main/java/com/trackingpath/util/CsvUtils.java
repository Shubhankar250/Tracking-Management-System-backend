package com.trackingpath.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.trackingpath.dtos.GpsPointDto;

public class CsvUtils {

    private CsvUtils() {
    }

    public static List<GpsPointDto> parseGpsCsv(InputStream inputStream) throws IOException {
        List<GpsPointDto> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (first) {
                    first = false;
                    if (line.toLowerCase().startsWith("lat")) {
                        continue;
                    }
                }

                String[] arr = line.split(",");
                if (arr.length < 3) {
                    continue;
                }

                GpsPointDto dto = new GpsPointDto();
                dto.setLatitude(Double.parseDouble(arr[0].trim()));
                dto.setLongitude(Double.parseDouble(arr[1].trim()));
                dto.setFixTime(LocalDateTime.parse(arr[2].trim()));

                if (arr.length >= 4 && !arr[3].trim().isEmpty()) {
                    dto.setSpeedKph(Double.parseDouble(arr[3].trim()));
                }

                list.add(dto);
            }
        }

        return list;
    }
}
