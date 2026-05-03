package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@Table(name = "users")
public class Users implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String accountname;
	@Column(name = "admin_id")
	private long adminId;
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
	@Column(columnDefinition = "TEXT")
	private String permissions;
	@Column(columnDefinition = "TEXT")
	private String objectlist;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Role> roles;

	private String lang_preferance;

	@Column(name = "server_name")
	private String serverName;

	@Column(name = "server_description")
	private String serverDescription;

	@Column(name = "default_date_format")
	private String defaultDateFormat;

	@Column(name = "default_time_format")
	private String defaultTimeFormat;

	@Column(name = "default_duration_format")
	private String defaultDurationFormat;

	@Column(name = "default_unit_of_distance")
	private String defaultUnitOfDistance;

	@Column(name = "default_unit_of_capacity")
	private String defaultUnitOfCapacity;

	@Column(name = "default_unit_of_altitude")
	private String defaultUnitOfAltitude;

	@Column(name = "no_reply_email_address")
	private String noReplyEmailAddress;

	@Column(name = "from_name")
	private String fromName;

	@Column(name = "frontpage_logo")
	private String frontpageLogo;

	private String favicon;

	@Column(name = "login_page_logo")
	private String loginPageLogo;

	@Column(name = "background_image")
	private String backgroundImage;

	@Column(name = "logintextcolor")
	private String loginTextColor;

	@Column(name = "loginpanelcolor")
	private String loginPanelColor;

	@Column(name = "welcometext")
	private String welcomeText;

	@Column(name = "bottomtext")
	private String bottomText;

	@Column(name = "applestorelink")
	private String appleStoreLink;

	@Column(name = "googleplaylink")
	private String googlePlayLink;

	@Column(name = "is_seen_by_user")
	private Boolean isSeenByUser;

	@Column(name = "first_login")
	private String firstLogin;

	@Column(name = "latitude")
	private String latitude;

	@Column(name = "longitude")
	private String longitude;

	@Column(name = "map_zoom_level")
	private String mapZoomLevel;

	@Column(name = "loginpaneltransparency")
	private String loginPanelTransparency;
	@Column(name = "available_subscription_points")
	private Integer availablesubscriptionpoints;
	
	@Column(name = "available_widgets")
	private String availableWidgets;

	@Column(name = "dashboard_menu")
	private String dashboardMenu;
	
	@Column(name = "sms_gateway_type")
	private String smsGatewayType;

	@Column(name = "sms_gateway_url")
	private String smsGatewayUrl;

	@Column(name = "smtp_host")
	private String smtpHost;

	@Column(name = "smtp_username")
	private String smtpUsername;

	@Column(name = "smtp_password")
	private String smtpPassword;

	@Column(name = "smtp_port")
	private String smtpPort;

	@Column(name = "smtp_encrption")
	private String smtpEncryption;

	@Column(name = "logged_in")
	private LocalDateTime loggedIn;

	@Column(name = "logged_out")
	private LocalDateTime loggedOut;
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled != null && enabled == 1;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles.stream().map(r -> new SimpleGrantedAuthority(r.getRoleName())).toList();
	}

}
