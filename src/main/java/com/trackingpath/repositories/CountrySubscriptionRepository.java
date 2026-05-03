package com.trackingpath.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trackingpath.entities.CountrySubscriptionEntity;

@Repository
public interface CountrySubscriptionRepository
        extends JpaRepository<CountrySubscriptionEntity, Long> {
}
