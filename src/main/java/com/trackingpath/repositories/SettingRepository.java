package com.trackingpath.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.trackingpath.entities.Users;


@Repository
public interface SettingRepository extends JpaRepository<Users, Long> {
	Users findByAdminIdAndIsSeenByUserFalse(Long adminId);

}
