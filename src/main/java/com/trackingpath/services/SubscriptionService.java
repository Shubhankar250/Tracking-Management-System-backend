package com.trackingpath.services;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.trackingpath.dtos.SubscriptionMasterDTO;
import com.trackingpath.entities.CountrySubscriptionEntity;
import com.trackingpath.entities.SubscriptionMasterEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.mapper.SubscriptionMapper;
import com.trackingpath.repositories.CountrySubscriptionRepository;
import com.trackingpath.repositories.SubscriptionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;




@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepo;
    private final CountrySubscriptionRepository countryRepo;
    private final SubscriptionMapper mapper;

    public Map<Long, String> getAllCountries() {
        return countryRepo.findAll()
                .stream()
                .collect(Collectors.toMap(
                        CountrySubscriptionEntity::getId,
                        CountrySubscriptionEntity::getCountryName,
                        (a,b) -> a,
                        LinkedHashMap::new
                ));
    }

    public void addSubscription(SubscriptionMasterDTO dto, Users user) {

        CountrySubscriptionEntity country =
                countryRepo.findById(dto.getCountrySubId())
                        .orElseThrow();

        double totalAmount = country.getPointValue() * dto.getSubPoints();

        SubscriptionMasterEntity e = new SubscriptionMasterEntity();
        mapper.updateEntity(e, dto, country);
        e.setTotalAmount(totalAmount);
        e.setCreatedBy(user);
        e.setCreatedOn(LocalDateTime.now());

        subscriptionRepo.save(e);
    }

    public Page<SubscriptionMasterDTO> getSubscriptions(Users user, String search, Pageable pageable) {

        boolean isUser = user.getRoles()
                .stream()
                .anyMatch(r -> "ROLE_USER".equalsIgnoreCase(r.getRoleName()));

        String country = isUser ? user.getCountry() : null;

        Page<SubscriptionMasterEntity> pageResult =
                subscriptionRepo.findAllByCountry(country, search, pageable);

        return pageResult.map(mapper::toDto);
    }

    public SubscriptionMasterDTO getById(Long id) {

        SubscriptionMasterEntity entity =
                subscriptionRepo.findByIdWithCountry(id)
                        .orElseThrow(() -> new RuntimeException("Subscription not found"));

        return mapper.toDto(entity);
    }


    public void updateSubscription(SubscriptionMasterDTO dto) {

        SubscriptionMasterEntity e =
                subscriptionRepo.findById(dto.getId()).orElseThrow();

        CountrySubscriptionEntity country =
                countryRepo.findById(dto.getCountrySubId()).orElseThrow();

        double totalAmount = country.getPointValue() * dto.getSubPoints();

        mapper.updateEntity(e, dto, country);
        e.setTotalAmount(totalAmount);

        subscriptionRepo.save(e);
    }

    @Transactional
    public boolean updateSubscriptionPoints(long userId, int points) {
        return subscriptionRepo.updateSubscriptionPoints(userId, points) > 0;
}
}