package com.trackingpath.services;

import com.trackingpath.dtos.ChildTripStatusResponse;
import com.trackingpath.dtos.LiveTrackingResponse;
import com.trackingpath.dtos.TripHistoryResponse;
import com.trackingpath.dtos.TripScheduleResponse;

public interface ParentTripService {
    LiveTrackingResponse getLiveTracking(Long passengerId);
    ChildTripStatusResponse getChildTripStatus(Long passengerId);
    TripScheduleResponse getSchedule(Long passengerId);
    TripHistoryResponse getHistory(Long passengerId, Integer page, Integer size);
}
