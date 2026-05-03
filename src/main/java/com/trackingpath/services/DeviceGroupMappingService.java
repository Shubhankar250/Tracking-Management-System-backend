package com.trackingpath.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackingpath.repositories.DeviceGroupMappingRepository;

@Service
@Transactional
public class DeviceGroupMappingService {

    private final DeviceGroupMappingRepository repository;

    public DeviceGroupMappingService(DeviceGroupMappingRepository repository) {
        this.repository = repository;
    }

  
}
