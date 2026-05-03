package com.trackingpath.dtos;

import lombok.Data;

@Data
public class SubscriptionMasterDTO {

    private Long id;
    private String subDetails;
    private Long subPoints;
    private Double totalAmount;
    private Long discount;

    private Long countrySubId;
    private String country;
}
