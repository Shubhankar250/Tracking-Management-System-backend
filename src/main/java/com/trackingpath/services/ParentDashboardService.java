package com.trackingpath.services;

import com.trackingpath.dtos.ParentChildrenResponse;
import com.trackingpath.dtos.ParentDashboardResponse;
import com.trackingpath.dtos.RouteLiveDataResponse;

public interface ParentDashboardService {
    ParentDashboardResponse getDashboard(Long passengerId);
    ParentChildrenResponse getChildren();
    RouteLiveDataResponse getLiveData();
}
