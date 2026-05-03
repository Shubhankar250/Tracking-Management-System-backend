package com.trackingpath.dtos;


import lombok.Data;
@Data
public class RegisterUserDto {

	private String accountname;
	private Long admin_id;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private String city;
    private String address;

    private Long roleId; // only ONE role
}
