package com.trackingpath.dtos;


import java.util.List;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private String phoneNumber;
    private Long groupId;
    private String city;
    private String country;
    private String timezone;
    private String accountname;
    private int enabled;
    private List<String> role; 
	private String password;
	private String address;
	private String access_type;
	private String available_maps;
	private String assign_device_ids;
	private String permissions;

	private String objectlist;
	private Integer available_subscription_points;
  
}
