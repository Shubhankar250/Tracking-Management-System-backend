package com.trackingpath.mapper;

import org.springframework.stereotype.Component;

import com.trackingpath.dtos.SubscriptionMasterDTO;
import com.trackingpath.entities.CountrySubscriptionEntity;
import com.trackingpath.entities.SubscriptionMasterEntity;

@Component
public class SubscriptionMapper {

    public SubscriptionMasterDTO toDto(SubscriptionMasterEntity e) {
        SubscriptionMasterDTO d = new SubscriptionMasterDTO();
        d.setId(e.getId());
        d.setSubDetails(e.getSubDetails());
        d.setSubPoints(e.getSubPoints());
        d.setTotalAmount(e.getTotalAmount());
        d.setDiscount(e.getDiscount());
        d.setCountry(e.getCountry().getCountryName());
        d.setCountrySubId(e.getCountry().getId());
        return d;
    }

    public void updateEntity(SubscriptionMasterEntity e, SubscriptionMasterDTO d, CountrySubscriptionEntity country) {
        e.setSubDetails(d.getSubDetails());
        e.setSubPoints(d.getSubPoints());
        e.setDiscount(d.getDiscount());
        e.setCountry(country);
    }
}
