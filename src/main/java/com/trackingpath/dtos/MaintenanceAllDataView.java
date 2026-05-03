package com.trackingpath.dtos;

import java.time.LocalDate;

public interface MaintenanceAllDataView {

    Long getId();
    String getServiceName();

    Long getDeviceId();
    String getDeviceName();

    Boolean getDatalist();
    Boolean getPopup();
    Boolean getOdometerIntervalKm();
    Boolean getEngineHourInterval();
    Boolean getDaysInterval();
    Boolean getOdometerLeftKm();
    Boolean getEngineHoursLeft();
    Boolean getUpdateLastService();
    Boolean getDaysLeft();
    Boolean getEventTrigger();

    Long getOdometerIntervalKmVal();
    Long getLastServiceKm();
    Long getEngineHourIntervalVal();
    Long getLastServiceHours();
    Long getDaysIntervalVal();
    Long getOdometerLeftKmVal();
    Long getEngineHoursLeftVal();
    Long getDaysLeftVal();

    // ✅ ADD THIS
    LocalDate getLastServiceDate();

    Long getUserId();
    String getUsername();
    Long getAdminId();
    String getAdminName();
}
