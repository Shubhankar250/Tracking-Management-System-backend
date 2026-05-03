package com.trackingpath.dtos;

import lombok.Data;

@Data
public class PickupDTO {
	    private Long id;
	    private String status;
	    private String picked_up_time;
	    private Double pickup_latitude;
	    private Double pickup_longitude;
	    private String pickupImage;

 }
