package com.trackingpath.mapper;


import java.time.LocalDateTime;

import com.trackingpath.dtos.DGMDTO;
import com.trackingpath.entities.GroupEntity;
import com.trackingpath.entities.Users;

public class GroupMapper {

    // Convert DGMBean + User → GroupEntity
    public static GroupEntity toEntity(DGMDTO bean, Users user,Users admin) {
        if (bean == null || user == null) return null;

        GroupEntity group = new GroupEntity();
        group.setName(bean.getGroup_name());
        group.setUser(user);
        group.setAdmin(admin);
        group.setCreationTime(LocalDateTime.now());

        return group;
    }
    
    
    public static GroupEntity toEntityForUpdate(DGMDTO bean, GroupEntity existingGroup) {
        if (bean == null || existingGroup == null) return existingGroup;

        existingGroup.setName(bean.getGroup_name());
        // optionally update other fields if needed
        return existingGroup;
    }
}
