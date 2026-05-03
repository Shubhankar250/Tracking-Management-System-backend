package com.trackingpath.impl;

import org.springframework.stereotype.Service;

import com.trackingpath.dtos.ParentProfileResponse;
import com.trackingpath.dtos.UpdateParentProfileRequest;
import com.trackingpath.dtos.SupportContactsResponse;
import com.trackingpath.entities.TransportPassenger;
import com.trackingpath.repositories.TransportPassengerRepository;
import com.trackingpath.services.ParentProfileService;
import com.trackingpath.util.SecurityUtils;

@Service
public class ParentProfileServiceImpl implements ParentProfileService {

    private final TransportPassengerRepository passengerRepository;

    public ParentProfileServiceImpl(TransportPassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    @Override
    public ParentProfileResponse getProfile() {

        TransportPassenger loginPassenger = getLoginPassenger();

        String guardianMobile = loginPassenger.getGuardianMobile();

        int childrenCount = passengerRepository
                .findByGuardianMobileAndActiveTrue(guardianMobile)
                .size();

        return ParentProfileResponse.builder()
                .parentId(loginPassenger.getId()) // logical parent
                .name(loginPassenger.getGuardianName())
                .mobile(loginPassenger.getGuardianMobile())
                .email(loginPassenger.getGuardianEmail())
                .childrenCount(childrenCount)
                .build();
    }

    @Override
    public void updateProfile(UpdateParentProfileRequest request) {

        TransportPassenger loginPassenger = getLoginPassenger();

        String guardianMobile = loginPassenger.getGuardianMobile();

        // 🔥 IMPORTANT: update ALL children of this parent
        var children = passengerRepository
                .findByGuardianMobileAndActiveTrue(guardianMobile);

        for (TransportPassenger p : children) {
            p.setGuardianName(request.getName());
            p.setGuardianMobile(request.getMobile());
            p.setGuardianEmail(request.getEmail());
        }

        passengerRepository.saveAll(children);
    }

    @Override
    public SupportContactsResponse getSupportContacts(Long passengerId) {

        // Optional: validate access
        TransportPassenger child = validateChildAccess(passengerId);

        return SupportContactsResponse.builder()
                .transportOffice(SupportContactsResponse.Contact.builder()
                        .name("School Transport Desk")
                        .mobile("+91-0000000000")
                        .email("transport@school.com")
                        .build())
                .driver(SupportContactsResponse.Contact.builder()
                        .name("TODO Driver")     // fetch from route/vehicle later
                        .mobile("+91-0000000001")
                        .build())
                .helper(SupportContactsResponse.Contact.builder()
                        .name("TODO Helper")
                        .mobile("+91-0000000002")
                        .build())
                .build();
    }

    // ================== HELPER METHODS ==================

    private TransportPassenger getLoginPassenger() {
        Long loginPassengerId = SecurityUtils.getCurrentUser().getPassengerLoginId();

        return passengerRepository.findById(loginPassengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found"));
    }

    private TransportPassenger validateChildAccess(Long passengerId) {

        TransportPassenger loginPassenger = getLoginPassenger();

        TransportPassenger child = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        if (!loginPassenger.getGuardianMobile().equals(child.getGuardianMobile())) {
            throw new SecurityException("You are not allowed to access this child");
        }

        return child;
    }
}