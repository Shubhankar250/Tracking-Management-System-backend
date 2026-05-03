package com.trackingpath.mapper;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.CustomUserDTO;
import com.trackingpath.dtos.UserDTO;
import com.trackingpath.entities.Role;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.RoleRepository;
import com.trackingpath.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor

public class UserMapper {

	@Autowired
	UserRepository usersRepository;

	private final PasswordEncoder passwordEncoder;
	private final RoleRepository roleRepository;

	public UserDTO toDto(Users user) {
		UserDTO dto = new UserDTO();
		dto.setId(user.getId());
		dto.setEmail(user.getEmail());
		dto.setPhoneNumber(user.getPhone_number1());
		dto.setAvailable_maps(user.getAvailable_maps());
		dto.setAvailable_subscription_points(user.getAvailablesubscriptionpoints());
		dto.setPermissions(user.getPermissions());
		dto.setAssign_device_ids(user.getAssign_device_ids());

		dto.setFirstname(user.getFirstname());
		dto.setLastname(user.getLastname());
		dto.setUsername(user.getUsername());
		dto.setRole(user.getRoles().stream().map(Role::getRoleName).toList());
		dto.setEnabled(user.getEnabled());
		dto.setAccess_type(user.getAccess_type());
		dto.setCountry(user.getCountry());
		dto.setTimezone(user.getTimezone());
		dto.setCity(user.getCity());
		dto.setAccountname(user.getAccountname());
		// role
		dto.setObjectlist(user.getObjectlist());
		return dto;
	}

	public List<UserDTO> toDtoList(List<Users> users) {
		return users.stream().map(this::toDto).collect(Collectors.toList());
	}

	public Users toEntity(CustomUserDTO bean, Users admin) {
		Users u = new Users();

		u.setFirstname(bean.getFirstname());
		u.setLastname(bean.getLastname());
		u.setUsername(bean.getUsername());
		u.setPassword(passwordEncoder.encode(bean.getPassword()));
		u.setEmail(bean.getEmail());
		u.setPhone_number1(bean.getPhone_number1());
		u.setCountry(bean.getCountry());
		u.setTimezone(bean.getTimezone());
		u.setAddress(bean.getAddress());
		u.setCity(bean.getCity());

		// Admin related fields
		u.setAccountname(admin.getUsername());
		u.setAdminId(admin.getId());
		u.setEnabled(bean.getEnabled());
		u.setAccess_type(bean.getAccess_type());
		u.setAvailable_maps(bean.getAvailable_maps());
		u.setAssign_device_ids(bean.getAssign_device_ids());
		u.setPermissions(bean.getPermissions());
		u.setFirstLogin("Y");
		u.setObjectlist(bean.getObjectlist() != null ? bean.getObjectlist() : "{}");

		u.setAvailablesubscriptionpoints(bean.getAvailable_subscription_points());
		Users savedUser = usersRepository.save(u);
		if (bean.getRole() != null && !bean.getRole().isEmpty()) {
			for (String roleName : bean.getRole()) {
				Role role = new Role();
				role.setRoleName(roleName); // ROLE_ADMIN, ROLE_USER
				role.setUser(savedUser); // FK user_id
				roleRepository.save(role);
			}
		}

		return u;
	}
	
	public void updateEntityFromDto(Users entity, CustomUserDTO dto) {
		
		// -----------------------------
		// Helper lambda for string check
		// -----------------------------
		Predicate<String> validString = s -> s != null && !s.trim().isEmpty()
				&& !s.trim().equals("0");

		// Update string fields
		if (validString.test(dto.getFirstname()))
			entity.setFirstname(dto.getFirstname());
		if (validString.test(dto.getLastname()))
			entity.setLastname(dto.getLastname());
		if (validString.test(dto.getUsername()))
			entity.setUsername(dto.getUsername());
		if (validString.test(dto.getEmail()))
			entity.setEmail(dto.getEmail());
		if (validString.test(dto.getPhone_number1()))
			entity.setPhone_number1(dto.getPhone_number1());
		if (validString.test(dto.getCountry()))
			entity.setCountry(dto.getCountry());
		if (validString.test(dto.getTimezone()))
			entity.setTimezone(dto.getTimezone());
		if (validString.test(dto.getAddress()))
			entity.setAddress(dto.getAddress());
		if (validString.test(dto.getCity()))
			entity.setCity(dto.getCity());
		if (validString.test(dto.getAccess_type()))
			entity.setAccess_type(dto.getAccess_type());
		if (validString.test(dto.getObjectlist()))
			entity.setObjectlist(dto.getObjectlist());

		// Update booleans only if not null
		if (dto.getEnabled() != null)
			entity.setEnabled(dto.getEnabled());

		// Update collections / JSON only if not null
		if (dto.getAvailable_maps() != null)
			entity.setAvailable_maps(dto.getAvailable_maps());
		if (dto.getAssign_device_ids() != null)
			entity.setAssign_device_ids(dto.getAssign_device_ids());
		if (dto.getPermissions() != null)
			entity.setPermissions(dto.getPermissions());

		// Password update only if provided
		if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
			entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		}

		// Numeric fields: update only if not null and greater than 0
		if (dto.getAvailable_subscription_points() != null && dto.getAvailable_subscription_points() > 0) {
			entity.setAvailablesubscriptionpoints(dto.getAvailable_subscription_points());
		}
	}
}
