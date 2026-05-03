package com.trackingpath.dtos;

import java.time.LocalDateTime;

public interface EventsView {

	
    LocalDateTime getAlertTime();
    String getAlertType();
}
