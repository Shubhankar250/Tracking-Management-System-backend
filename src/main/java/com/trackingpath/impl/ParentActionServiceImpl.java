package com.trackingpath.impl;

import org.springframework.stereotype.Service;
import com.trackingpath.dtos.EmergencyAlertRequest;
import com.trackingpath.dtos.MarkAbsenceRequest;
import com.trackingpath.dtos.SupportMessageRequest;
import com.trackingpath.entities.ParentAbsenceRequest;
import com.trackingpath.entities.ParentEmergencyAlert;
import com.trackingpath.entities.ParentSupportTicket;
import com.trackingpath.repositories.ParentAbsenceRequestRepository;
import com.trackingpath.repositories.ParentEmergencyAlertRepository;
import com.trackingpath.repositories.ParentSupportTicketRepository;
import com.trackingpath.services.ParentActionService;
import com.trackingpath.util.SecurityUtils;

@Service
public class ParentActionServiceImpl implements ParentActionService {

    private final ParentAbsenceRequestRepository absenceRequestRepository;
    private final ParentSupportTicketRepository supportTicketRepository;
    private final ParentEmergencyAlertRepository emergencyAlertRepository;

    public ParentActionServiceImpl(ParentAbsenceRequestRepository absenceRequestRepository,
                                   ParentSupportTicketRepository supportTicketRepository,
                                   ParentEmergencyAlertRepository emergencyAlertRepository) {
        this.absenceRequestRepository = absenceRequestRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.emergencyAlertRepository = emergencyAlertRepository;
    }

    @Override
    public Long markAbsence(MarkAbsenceRequest request) {
        ParentAbsenceRequest entity = new ParentAbsenceRequest();
        entity.setParentId(SecurityUtils.getCurrentUser().getParentId());
        entity.setPassengerId(request.getPassengerId());
        entity.setTripDate(request.getTripDate());
        entity.setTripType(request.getTripType());
        entity.setReason(request.getReason());
        return absenceRequestRepository.save(entity).getId();
    }

    @Override
    public Long sendSupportMessage(SupportMessageRequest request) {
        ParentSupportTicket entity = new ParentSupportTicket();
        entity.setParentId(SecurityUtils.getCurrentUser().getParentId());
        entity.setPassengerId(request.getPassengerId());
        entity.setSubject(request.getSubject());
        entity.setMessage(request.getMessage());
        entity.setStatus("OPEN");
        return supportTicketRepository.save(entity).getId();
    }

    @Override
    public Long raiseEmergencyAlert(EmergencyAlertRequest request) {
        ParentEmergencyAlert entity = new ParentEmergencyAlert();
        entity.setParentId(SecurityUtils.getCurrentUser().getParentId());
        entity.setPassengerId(request.getPassengerId());
        entity.setTripId(request.getTripId());
        entity.setAlertType(request.getAlertType());
        entity.setMessage(request.getMessage());
        return emergencyAlertRepository.save(entity).getId();
    }
}
