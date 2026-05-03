package com.trackingpath.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.trackingpath.entities.Driveres;
import com.trackingpath.entities.TransportPassenger;
import com.trackingpath.entities.TransportPassengerRouteAssignment;
import com.trackingpath.repositories.DriverRepository;
import com.trackingpath.repositories.TransportPassengerRepository;
import com.trackingpath.repositories.TransportPassengerRouteAssignmentRepository;

@Service
public class DriverUserDetailsService implements UserDetailsService {

    private final DriverRepository  driverRepository;
   

    public DriverUserDetailsService(
    		DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
       ;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Fetch login record
    	Driveres driver =
        		driverRepository.findDriverByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Driver login not found"));

        
             
        // 4. Build principal
        return new CustomDriverUserPrincipal(               
                driver.getId(),
                driver.getUsername(),
                driver.getPassword(), // encoded password
                "PARENT",
                Boolean.TRUE.equals(driver.getActive())
        );
    }
}