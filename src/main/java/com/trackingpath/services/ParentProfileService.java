package com.trackingpath.services;

import com.trackingpath.dtos.ParentProfileResponse;
import com.trackingpath.dtos.UpdateParentProfileRequest;
import com.trackingpath.dtos.SupportContactsResponse;

public interface ParentProfileService {
    ParentProfileResponse getProfile();
    void updateProfile(UpdateParentProfileRequest request);
    SupportContactsResponse getSupportContacts(Long passengerId);
}
