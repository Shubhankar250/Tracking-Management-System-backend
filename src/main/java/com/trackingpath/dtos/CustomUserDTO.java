package com.trackingpath.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDTO {
	private Long id;
	private String firstname;
	private String lastname;
	private String username;
	private String password;
	private String email;
	private String phone_number1;
	private String country;
	private String timezone;
	private String address;
	private String city;

	private Integer enabled;
	private String access_type;
	private String available_maps;
	private String assign_device_ids;
	private String permissions;

	private String objectlist;
	private Integer available_subscription_points;
    private List<String> role;
    
    private String smsGatewayType;
    private String smsGatewayUrl;

    private String smtpHost;
    private String smtpUsername;
    private String smtpPassword;
    private String smtpPort;
    private String smtpEncryption;

    private String availableWidgets;
    private String dashboardMenu;
    
    public CustomUserDTO(String smsGatewayType, String smsGatewayUrl, String smtpHost, String smtpUsername,
			String smtpPassword, String smtpPort, String smtpEncryption, String availableWidgets,String dashboardMenu,Integer available_subscription_points,String timezone
			) {
		this.smsGatewayType = smsGatewayType;
		this.smsGatewayUrl = smsGatewayUrl;
		this.smtpHost = smtpHost;
		this.smtpUsername = smtpUsername;
		this.smtpPassword = smtpPassword;
		this.smtpPort = smtpPort;
		this.smtpEncryption = smtpEncryption;
		this.availableWidgets = availableWidgets;
		this.dashboardMenu = dashboardMenu;
		this.available_subscription_points=available_subscription_points;
		this.timezone=timezone;
		
	}
}
