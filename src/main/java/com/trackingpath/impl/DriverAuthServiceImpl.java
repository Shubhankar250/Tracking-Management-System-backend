package com.trackingpath.impl;

import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.trackingpath.dtos.*;
import com.trackingpath.entities.Driveres;
import com.trackingpath.entities.TransportPassenger;
import com.trackingpath.entities.TransportPassengerRouteAssignment;
import com.trackingpath.repositories.DriverRepository;
import com.trackingpath.repositories.TransportPassengerRepository;
import com.trackingpath.repositories.TransportPassengerRouteAssignmentRepository;
import com.trackingpath.security.CustomDriverUserPrincipal;
import com.trackingpath.security.CustomUserPrincipal;
import com.trackingpath.security.JwtDriverService;
import com.trackingpath.security.JwtProperties;
import com.trackingpath.security.JwtParentService;
import com.trackingpath.services.DriverAuthService;
import com.trackingpath.services.ParentAuthService;
import com.trackingpath.util.SecurityUtils;

import jakarta.transaction.Transactional;

@Service
public class DriverAuthServiceImpl implements DriverAuthService {

    private final AuthenticationManager authenticationManager;
    private final DriverRepository  driverRepository;
    private final JwtDriverService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    public DriverAuthServiceImpl(AuthenticationManager authenticationManager,
    		DriverRepository driverRepository,                               
    		JwtDriverService jwtService,
                                 JwtProperties jwtProperties,
                                 PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.driverRepository = driverRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public DriverLoginResponse login(DriverLoginRequest request) {

        // 1. Authenticate
    	System.out.println("driver===---"+request.getUsername()+"---"+ request.getPassword());
  
        // 2. Get assignment (login table)
    	Driveres driver =
    			driverRepository.findDriverByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid userssname or password"));

      	if (!passwordEncoder.matches(request.getPassword(), driver.getPassword())) {
    	    throw new BadCredentialsException("Invalid password");
    	}
       
        // 6. Custom principal
      	CustomDriverUserPrincipal principal = new CustomDriverUserPrincipal(
        		driver.getId(),             
        		driver.getUsername(),
        		driver.getPassword(),
                request.getRole(),
            true
        );

        return DriverLoginResponse.builder()
                .token(jwtService.generateToken(principal))
                .tokenType("Bearer")
                .expiresInSeconds(jwtProperties.expirationSeconds()) 
                .driverId(driver.getId())
                .username(driver.getUsername())
                .build();
    }
    
    @Override
    public DriverMeResponse me() {
    	CustomDriverUserPrincipal user = SecurityUtils.getCurrentDriver();
       
    	 Driveres driver = driverRepository
    	            .findDriverByUsername(user.getUsername())
    	            .orElseThrow();
        return DriverMeResponse.builder()
                .driverId(user.getDriverId())
                .username(user.getUsername())
                .role("DRIVER")
                .name(driver.getName())
                .mobile(driver.getPhone())
                .email(driver.getEmail())
                .build();
    }
    
    @Override
    public void changePassword(ChangePasswordRequest request) {

        CustomUserPrincipal user = SecurityUtils.getCurrentUser();

        Driveres driver =
        		driverRepository.findDriverByUsername(user.getUsername())
                .orElseThrow();

        if (!passwordEncoder.matches(request.getOldPassword(), driver.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        driver.setPassword(passwordEncoder.encode(request.getNewPassword()));
        driverRepository.save(driver);
    }

	@Override
	public void logout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forgotPassword(ForgotPasswordRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetPassword(ResetPasswordRequest request) {
		// TODO Auto-generated method stub
		
	}
}