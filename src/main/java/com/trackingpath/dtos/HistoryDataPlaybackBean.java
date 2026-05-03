package com.trackingpath.dtos;

import lombok.Data;
import java.util.List;

@Data
public class HistoryDataPlaybackBean {

    private List<EventDataBean> eventDataList;
    private long total_running_time;
    private double distance;

    private List<ParkingDataBean> parkingDataList;
    private List<Object> combinedPlaybackList;

    private List<LiveDataBeanForCard> liveDataBeanForCard;
}
