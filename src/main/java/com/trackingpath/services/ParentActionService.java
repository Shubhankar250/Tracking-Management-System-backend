package com.trackingpath.services;

import com.trackingpath.dtos.EmergencyAlertRequest;
import com.trackingpath.dtos.MarkAbsenceRequest;
import com.trackingpath.dtos.SupportMessageRequest;

public interface ParentActionService {
    Long markAbsence(MarkAbsenceRequest request);
    Long sendSupportMessage(SupportMessageRequest request);
    Long raiseEmergencyAlert(EmergencyAlertRequest request);
}
