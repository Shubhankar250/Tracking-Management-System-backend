package com.trackingpath.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.dtos.SubscriptionMasterDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.SubscriptionService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final AuthenticationService authenticationService;
    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/countries")
    public Map<Long, String> countries() {
        return subscriptionService.getAllCountries();
    }

    @GetMapping
    public ResponseEntity<Page<SubscriptionMasterDTO>> list(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "25") int size,
            @RequestParam(required = false, defaultValue = "") String search
    ) {
        Users user = authenticationService.getUser();
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(subscriptionService.getSubscriptions(user, search, pageable));
    }

    @GetMapping("/{id}")
    public SubscriptionMasterDTO get(@PathVariable Long id) {
        return subscriptionService.getById(id);
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody SubscriptionMasterDTO dto,HttpServletRequest request) {
        subscriptionService.addSubscription(dto, authenticationService.getUser());
        Users user = authenticationService.getUser();
        activityLogService.createActivity("SUBSCRIPTION CREATED",
				"SUBSCRIPTION WITH POINT (" + dto.getSubPoints() + ") CREATED BY " + user.getUsername(), user, request);
        return ResponseEntity.ok("Added Successfully");
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody SubscriptionMasterDTO dto,HttpServletRequest request) {
        subscriptionService.updateSubscription(dto);
        Users user = authenticationService.getUser();
        activityLogService.createActivity("SUBSCRIPTION UPDATED",
				"SUBSCRIPTION WITH POINT (" + dto.getSubPoints() + ") UPDATED BY " + user.getUsername(), user, request);
        return ResponseEntity.ok("Updated Successfully");
    }
    @PutMapping("/updateSubscriptionPoints")
    public ResponseEntity<String> updateSubscriptionPoints(
            @RequestParam("points") int points,HttpServletRequest request) {

        Users user = authenticationService.getUser();
        boolean status = subscriptionService.updateSubscriptionPoints(user.getId(), points);
        
        activityLogService.createActivity("PAYMENT",
				"PAYMENT WITH POINT (" +points + ") DONE BY " + user.getUsername(), user, request);

        return ResponseEntity.ok(status ? "Payment Done Successfully" : "Failed!");
    }
}

