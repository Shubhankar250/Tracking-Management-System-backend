package com.trackingpath.controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.configs.Constant;
import com.trackingpath.dtos.CustomUserDTO;
import com.trackingpath.dtos.DeviceForUserBean;
import com.trackingpath.dtos.UserDTO;
import com.trackingpath.dtos.UserModulePermission;
import com.trackingpath.entities.Users;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private ActivityLogService activityLogService;
	@Value("${AVAILABLE_MAPS}")
	private String availableMaps;
	@Value("${DEFAULT_SELECTED_MAPS}")
	private String defaultSelectedMaps;
	// ================= USER APIs =================

	// CREATE USER
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> createUser(@RequestBody CustomUserDTO bean,
			@RequestParam(name = "group_id", defaultValue = "0") long groupId, HttpServletRequest request) {

		Users admin = authenticationService.getUser();
		Long id = userService.createUser(bean, admin);

		if (id != null && id > 0) {
			if (groupId > 0) {
				userService.assignGroup(admin.getAdminId(), groupId, id);
			}
			  // ACTIVITY LOG — USER CREATED
			activityLogService.createActivity("NEW USER CREATED",
					"USER(" + bean.getUsername() + ") CREATED BY " + bean.getUsername(), admin, request);
			return ResponseEntity.ok("New user saved successfully!");
		}
		return ResponseEntity.badRequest().body("Failed to create user");
	}

	// UPDATE USER
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updateUser(@RequestBody CustomUserDTO bean,
			@RequestParam(name = "group_id", defaultValue = "0") long groupId,HttpServletRequest request) {

		Users admin = authenticationService.getUser();
		long status = userService.update(bean, admin);

		if (status > 0) {
			if (groupId > 0) {
				userService.updateUserGroup(bean.getId(), groupId, admin.getAdminId());
			}
			activityLogService.createActivity("USER UPDATED",
					"USER(" + bean.getUsername() + ") UPDATED BY " + bean.getUsername(), admin, request);
			return ResponseEntity.ok("Details updated successfully!");
		}
		return ResponseEntity.badRequest().body("Failed to update user");
	}

	// DELETE USER
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable Long id,HttpServletRequest request) {
		Users admin = authenticationService.getUser();

		boolean status = userService.deleteUser(id);
		if (status) {
			activityLogService.createActivity("USER DELETED",
				"USER DELETED WITH ID (" +id + ") DELETED BY " + admin.getUsername(), admin, request);
		}
		return status ? ResponseEntity.ok("User deleted successfully!")
				: ResponseEntity.badRequest().body("Failed to delete user");
	}

	// GET USER BY ID
	   @GetMapping("/{id}")
	    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
	        Long userId;
	           if (id == 0) {
	                   Users user = authenticationService.getUser();
	                   userId = user.getId();
	           } else {
	                   userId = id;
	           }
	        return ResponseEntity.ok(userService.getUserByIdSimple(userId));
	    }


	// GET ALL USERS
	@GetMapping
	public ResponseEntity<Page<UserDTO>> getAllUsers(
	        @RequestParam(required = false, defaultValue = "0") int page,
	        @RequestParam(required = false, defaultValue = "25") int size,
	        @RequestParam(required = false, defaultValue = "") String search
	) {
	    Users user = authenticationService.getUser();
	    Pageable pageable = PageRequest.of(page, size);

	    return ResponseEntity.ok(userService.getUserByAdmin_id(user, pageable, search));
	}

	@GetMapping("/permission")
	public ResponseEntity<List<UserModulePermission>> getDefaultPermissions() {
		List<UserModulePermission> permissionList = Constant.getDefaultPermission();
		return ResponseEntity.ok(permissionList);
	}

	@GetMapping("/maps")
	public ResponseEntity<Map<String, Object>> getMapSettings() {

		List<String> availableMapsList = Arrays.asList(availableMaps.split(","));

		Set<String> defaultSelectedSet = new HashSet<>(Arrays.asList(defaultSelectedMaps.split(",")));

		Map<String, Object> response = new HashMap<>();
		response.put("availableMaps", availableMapsList);
		response.put("defaultSelectedMaps", defaultSelectedSet);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/devices/grouped")
	public ResponseEntity<Map<String, List<DeviceForUserBean>>> getDevicesWithGroup() {
		return ResponseEntity.ok(userService.fetchAllDevicesWithGroup());
	}

	@GetMapping("/getuserdata")
	public ResponseEntity<Map<String, Object>> getUserData() {
		Users user = authenticationService.getUser();
		Map<String, Object> response = userService.getUserData(user);
		return ResponseEntity.ok(response);
	}
	
	   @GetMapping("/allUser")
	    public ResponseEntity<Map<Long, String>> getAllUser() {

	           Users User = authenticationService.getUser();

	           Map<Long, String> usersMap = userService.getAllUser(User);

	           return ResponseEntity.ok(usersMap);
	    }
}
