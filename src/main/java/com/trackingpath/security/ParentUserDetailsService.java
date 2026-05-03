package com.trackingpath.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.trackingpath.entities.TransportPassenger;
import com.trackingpath.entities.TransportPassengerRouteAssignment;
import com.trackingpath.repositories.TransportPassengerRepository;
import com.trackingpath.repositories.TransportPassengerRouteAssignmentRepository;

@Service
public class ParentUserDetailsService implements UserDetailsService {

    private final TransportPassengerRouteAssignmentRepository assignmentRepository;
    private final TransportPassengerRepository passengerRepository;

    public ParentUserDetailsService(
            TransportPassengerRouteAssignmentRepository assignmentRepository,
            TransportPassengerRepository passengerRepository) {
        this.assignmentRepository = assignmentRepository;
        this.passengerRepository = passengerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Fetch login record
        TransportPassengerRouteAssignment assignment =
                assignmentRepository.findByUsernameAndActiveTrue(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Parent login not found"));

        // 2. Fetch passenger (child)
        TransportPassenger passenger = assignment.getPassenger();

        if (passenger == null) {
            throw new UsernameNotFoundException("Passenger not linked with this login");
        }

        // 3. Derive parentId (using guardian identity)
        Long parentId = passenger.getId(); // logical parent (see note below)

        // 4. Build principal
        return new CustomUserPrincipal(
                parentId,
                passenger.getId(),
                assignment.getUsername(),
                assignment.getTempPassword(), // encoded password
                "PARENT",
                Boolean.TRUE.equals(assignment.getActive())
        );
    }
}