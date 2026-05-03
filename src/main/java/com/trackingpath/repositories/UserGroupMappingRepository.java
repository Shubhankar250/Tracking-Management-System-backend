package com.trackingpath.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.UserGroupMapping;

public interface UserGroupMappingRepository extends JpaRepository<UserGroupMapping, Long> {
	
	Optional<UserGroupMapping> findByUserId(long userId);
	
}
