package com.trackingpath.services;

import com.trackingpath.dtos.LoginUserDto;
import com.trackingpath.dtos.RegisterUserDto;
import com.trackingpath.entities.Role;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.RoleRepository;
import com.trackingpath.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final RoleRepository roleRepository;

	public Users authenticate(LoginUserDto input) {

		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(input.getUsername(), input.getPassword()));

		// find user normally (because return type is Users, not Optional)
		Users user = userRepository.findByUsername(input.getUsername());

		if (user == null) {
			throw new RuntimeException("User not found");
		}

		return user;
	}

	

	public Users signup(RegisterUserDto dto) {

		Users user = new Users();
		user.setAccountname(dto.getAccountname());
		user.setAdminId(dto.getAdmin_id());
		user.setFirstname(dto.getFirstname());
		user.setLastname(dto.getLastname());
		user.setUsername(dto.getUsername());
		user.setEmail(dto.getEmail());
		user.setCity(dto.getCity());
		user.setAddress(dto.getAddress());
		user.setPhone_number1(dto.getPhoneNumber());
		user.setEnabled(1);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));

		// Assign SINGLE ROLE
		System.out.println("role_id=" + dto.getRoleId());

		Role role = roleRepository.findById(dto.getRoleId()).orElseThrow(() -> new RuntimeException("Role not found"));
		if (role == null)
			throw new RuntimeException("Role not found: " + dto.getRoleId());

		/*
		 * user.setRoles(role);
		 */
		return userRepository.save(user);
	}

	public Users getUser() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		
		Users user = userRepository.findByUsername(username);
		System.out.println("usernae--" + username);
		if (user == null) {
			throw new RuntimeException("User not found");
		}
		return user;
	}

	public static Users getCurrentUser() {
		return (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
}
