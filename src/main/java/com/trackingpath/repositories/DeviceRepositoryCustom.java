package com.trackingpath.repositories;

import java.util.Map;

import com.trackingpath.entities.Users;

public interface DeviceRepositoryCustom {
    Map<String, Object> getAlldataobject(Users user, int draw, int start, int length, String search);
}
