package com.trackingpath.dtos;

import lombok.Data;
import java.util.List;

@Data
public class HistoryDataPlaybackDTO {

    private List<EventDataDTO> eventDataList;
    private long total_running_time;
    private double distance;

    private List<ParkingDataBean> parkingDataList;
    private List<Object> combinedPlaybackList;
    private BillingSummary summary;

    private List<LiveDataDTOForCard> liveDataBeanForCard;
}
