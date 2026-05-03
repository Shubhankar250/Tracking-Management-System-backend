package com.trackingpath.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.*;

import java.util.ArrayList;
import java.util.List;

public class HistoryGSRView {

    public static byte[] generate(HistoryDataPlaybackDTO data) throws Exception {

        List<EventDataDTO> eventDataList = data.getEventDataList();
        List<Object> combinedList = data.getCombinedPlaybackList();

        // 🔹 Extract Parking Data
        List<ParkingDataBean> parkingDataList = new ArrayList<>();
        for (Object obj : combinedList) {
            if (obj instanceof ParkingDataBean) {
                parkingDataList.add((ParkingDataBean) obj);
            }
        }

        // 🔹 Route List
        List<HistoryGSRReportDTO.RoutePoint> routeList = new ArrayList<>();
        for (EventDataDTO event : eventDataList) {

            HistoryGSRReportDTO.RoutePoint point =
                    HistoryGSRReportDTO.RoutePoint.builder()
                            .lat(event.getLatitude())
                            .lon(event.getLongitude())
                            .deviceTime(event.getDeviceTime())
                            .build();

            routeList.add(point);
        }

        // 🔹 Stop List
        List<HistoryGSRReportDTO.StopInfo> stopList = new ArrayList<>();
        for (ParkingDataBean park : parkingDataList) {

            HistoryGSRReportDTO.StopInfo stop =
                    HistoryGSRReportDTO.StopInfo.builder()
                            .startTime(park.getParking_start_time().toString())
                            .endTime(park.getParking_end_time().toString())
                            .duration(park.getTime())
                            .build();

            stopList.add(stop);
        }

        // 🔹 Build Final GSR Object
        HistoryGSRReportDTO gsr = new HistoryGSRReportDTO();

        if (!eventDataList.isEmpty() && eventDataList.get(0).getDetails() != null) {

            DevicePlaybackDto details = eventDataList.get(0).getDetails();
            gsr.setName(details.getName());

        } else {
            gsr.setImei("");
            gsr.setName("");
        }

        // IMPORTANT: set route & stops separately
        gsr.setRoute(routeList);
        gsr.setStops(stopList);

        // 🔹 Convert to JSON bytes
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(gsr);
    }
}
