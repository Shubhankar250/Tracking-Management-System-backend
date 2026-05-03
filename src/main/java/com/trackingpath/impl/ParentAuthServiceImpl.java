package com.trackingpath.impl;

import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.trackingpath.dtos.*;
import com.trackingpath.entities.TransportPassenger;
import com.trackingpath.entities.TransportPassengerRouteAssignment;
import com.trackingpath.repositories.TransportPassengerRepository;
import com.trackingpath.repositories.TransportPassengerRouteAssignmentRepository;
import com.trackingpath.security.CustomUserPrincipal;
import com.trackingpath.security.JwtProperties;
import com.trackingpath.security.JwtParentService;
import com.trackingpath.services.ParentAuthService;
import com.trackingpath.util.SecurityUtils;

import jakarta.transaction.Transactional;

@Service
public class ParentAuthServiceImpl implements ParentAuthService {

    private final AuthenticationManager authenticationManager;
    private final TransportPassengerRepository passengerRepository;
    private final TransportPassengerRouteAssignmentRepository assignmentRepository;
    private final JwtParentService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    public ParentAuthServiceImpl(AuthenticationManager authenticationManager,
                                 TransportPassengerRepository passengerRepository,
                                 TransportPassengerRouteAssignmentRepository assignmentRepository,
                                 JwtParentService jwtService,
                                 JwtProperties jwtProperties,
                                 PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.passengerRepository = passengerRepository;
        this.assignmentRepository = assignmentRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public ParentLoginResponse login(ParentLoginRequest request) {

        // 1. Authenticate
    	System.out.println("feee===---"+request.getUsername()+"---"+ request.getPassword());
  
        // 2. Get assignment (login table)
        TransportPassengerRouteAssignment assignment =
            assignmentRepository.findByUsernameAndActiveTrue(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid userssname or password"));

      	if (!passwordEncoder.matches(request.getPassword(), assignment.getTempPassword())) {
    	    throw new BadCredentialsException("Invalid password");
    	}

        // 3. Get passenger (child)
        TransportPassenger passenger = assignment.getPassenger();

        // 4. Parent identity (derived from guardian)
        String guardianMobile = passenger.getGuardianMobile();

        // 5. Fetch all children of same parent
        List<ChildBasicDto> children = passengerRepository
            .findByGuardianMobileAndActiveTrue(guardianMobile)
            .stream()
            .map(p -> ChildBasicDto.builder()
                    .passengerId(p.getId())
                    .passengerName(p.getPassengerName())
                    .build())
            .toList();

        // 6. Custom principal
        CustomUserPrincipal principal = new CustomUserPrincipal(
            passenger.getId(),   // parentId (logical)
            passenger.getId(),   // loginId
            assignment.getUsername(),
            assignment.getTempPassword(),
            request.getRole(),
            true
        );

        return ParentLoginResponse.builder()
                .token(jwtService.generateToken(principal))
                .tokenType("Bearer")
                .expiresInSeconds(jwtProperties.expirationSeconds())
                .parent(ParentSummaryDto.builder()
                        .parentId(passenger.getId())
                        .passengerLoginId(passenger.getId())
                        .name(passenger.getGuardianName())
                        .mobile(passenger.getGuardianMobile())
                        .email(passenger.getGuardianEmail())
                        .build())
                .children(children)
                .build();
    }
    
    @Override
    public ParentMeResponse me() {
        CustomUserPrincipal user = SecurityUtils.getCurrentUser();

        TransportPassenger passenger = passengerRepository
                .findById(user.getPassengerLoginId())
                .orElseThrow();

        return ParentMeResponse.builder()
                .parentId(user.getParentId())
                .username(user.getUsername())
                .role("PARENT")
                .name(passenger.getGuardianName())
                .mobile(passenger.getGuardianMobile())
                .email(passenger.getGuardianEmail())
                .build();
    }
    
    @Override
    public void changePassword(ChangePasswordRequest request) {

        CustomUserPrincipal user = SecurityUtils.getCurrentUser();

        TransportPassengerRouteAssignment assignment =
            assignmentRepository.findByUsername(user.getUsername())
                .orElseThrow();

        if (!passwordEncoder.matches(request.getOldPassword(), assignment.getTempPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        assignment.setTempPassword(passwordEncoder.encode(request.getNewPassword()));
        assignmentRepository.save(assignment);
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