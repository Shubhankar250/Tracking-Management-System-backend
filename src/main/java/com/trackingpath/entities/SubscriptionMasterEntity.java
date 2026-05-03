package com.trackingpath.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscription_master")
@Data
public class SubscriptionMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sub_details")
    private String subDetails;

    @Column(name = "sub_points")
    private Long subPoints;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "discount")
    private Long discount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_sub_id")
    private CountrySubscriptionEntity country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @Column(name = "created_on")
    private LocalDateTime createdOn;
}

