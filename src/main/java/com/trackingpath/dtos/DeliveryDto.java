package com.trackingpath.dtos;

import lombok.Data;
@Data
public class DeliveryDto {
	    private Long id;
	    private String status;
	    private String delivered_time;
	    private Double delivery_latitude;
	    private Double delivery_longitude;
	    private String deliveryImage;
	}