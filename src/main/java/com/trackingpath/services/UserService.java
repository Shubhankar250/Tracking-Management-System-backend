package com.trackingpath.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.trackingpath.dtos.CustomUserDTO;
import com.trackingpath.dtos.DeviceForUserBean;
import com.trackingpath.dtos.UserDTO;
import com.trackingpath.entities.UserGroupMapping;
import com.trackingpath.entities.Users;
import com.trackingpath.mapper.UserMapper;
import com.trackingpath.repositories.UserGroupMappingRepository;
import com.trackingpath.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired
	UserRepository usersRepository;
	@Autowired
	AuthenticationService authenticationService;
	@Autowired
	UserMapper userMapper;
	@Autowired
	private UserGroupMappingRepository groupRepo;

	public Long createUser(CustomUserDTO bean, Users admin) {

		Users newUser = userMapper.toEntity(bean, admin);

		//Users saved = usersRepository.save(newUser);

		return newUser.getId();
	}

	public void assignGroup(long admin_id, long group_id, long user_id) {
		UserGroupMapping m = new UserGroupMapping();
		m.setAdminId(admin_id);
		m.setGroupId(group_id);
		m.setUserId(user_id);
		groupRepo.save(m);
	}

	public long update(CustomUserDTO dto, Users loggedInAdmin) {

		Users existing = usersRepository.findById(dto.getId())
				.orElseThrow(() -> new RuntimeException("User not found: " + dto.getId()));

		// Update fields
		userMapper.updateEntityFromDto(existing, dto);

		// Update admin tracking
		existing.setAdminId(loggedInAdmin.getId());
		existing.setAccountname(loggedInAdmin.getUsername());

		// Save
		Users saved = usersRepository.save(existing);
		return saved.getId();
	}

	public void updateUserGroup(long userId, long groupId, Long adminId) {

	    Optional<UserGroupMapping> existing = groupRepo.findByUserId(userId);

	    if (existing.isPresent()) {
	        UserGroupMapping mapping = existing.get();
	        mapping.setGroupId(groupId);
	        mapping.setAdminId(adminId);

	        groupRepo.save(mapping); 
	    } else {
	        UserGroupMapping mapping = new UserGroupMapping();
	        mapping.setUserId(userId);
	        mapping.setGroupId(groupId);
	        mapping.setAdminId(adminId);

	        groupRepo.save(mapping);
	    }
	}

	public boolean deleteUser(Long id) {
		if (usersRepository.existsById(id)) {
			usersRepository.deleteById(id);
			return true;
		}
		return false;
	}

	@Transactional
	public Page<UserDTO> getUserByAdmin_id(Users user, Pageable pageable, String search) {

		String searchParam = (search != null ? search.trim() : "");
		
	    boolean isAdmin = user.getRoles().stream()
	            .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

	    Long adminId = isAdmin ? null : user.getId();

	    Page<Users> usersPage =
	            usersRepository.findUsersExcludingAdmins(adminId, searchParam, pageable);

	    return usersPage.map(userMapper::toDto);
	}




	public UserDTO getUserByIdSimple(Long id) {
		Users user = usersRepository.findWithRolesById(id)
	            .orElseThrow(() -> new RuntimeException("User not found: " + id));		if (user == null) {
			return null;
		}
		return userMapper.toDto(user);

	}
	
	  public Map<Long, String> getAllUsers(Users user) {
	        Map<Long, String> results = new LinkedHashMap<>();

	        if (user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()))) {
	            List<Users> users = usersRepository.findByAdminId(user.getId());
	            for (Users u : users) {
	                if (!u.getId().equals(user.getId())) { // exclude admin itself
	                    results.put(u.getId(), u.getUsername() + "[ " + u.getFirstname() + " " + u.getLastname() + " ]");
	                }
	            }
	        } else {
	            // Normal user - return only self
	            Optional<Users> optionalUser = usersRepository.findById(user.getId());
	            optionalUser.ifPresent(u -> 
	                results.put(u.getId(), u.getUsername() + "[ " + u.getFirstname() + " " + u.getLastname() + " ]")
	            );
	        }

	        return results;
	    }
	  @Transactional
	    public boolean updateToken(String username, String token) {
	        int updated = usersRepository.updateResetToken(username, token);
	        return updated > 0;
	    }

	   @Transactional
	    public boolean newPasswordReset(String newPassword, String token) {
	        int updated = usersRepository.newPasswordReset(newPassword, token);
	        return updated > 0;
	    }

	    @Transactional
	    public boolean updateDisableStatus(String token) {
	        int updated = usersRepository.updateDisableStatus(token);
	        return updated > 0;
	    }

		public boolean getEnableStatus(String token) {
	        boolean updated = usersRepository.getEnableStatus(token);
	        return updated;
		}

		public Map<String, List<DeviceForUserBean>> fetchAllDevicesWithGroup() {

		    Users user = authenticationService.getCurrentUser();

		    boolean isAdmin = user.getRoles().stream()
		            .anyMatch(r -> r.getRoleName().equals("ROLE_ADMIN"));

		    List<Long> deviceIds = new ArrayList<>();

		    if (user.getAssign_device_ids() != null && !user.getAssign_device_ids().isBlank()) {
		        deviceIds = Arrays.stream(user.getAssign_device_ids().split(","))
		                .map(String::trim)
		                .map(Long::valueOf)
		                .toList();
		    }
		    
		    if (deviceIds.isEmpty()) {
		        deviceIds = List.of(-1L); // safe dummy value
		    }

		    List<Object[]> rows = usersRepository.fetchDevicesWithGroup(
		            user.getId(),
		            isAdmin,
		            deviceIds
		    );

		    Map<String, List<DeviceForUserBean>> result = new LinkedHashMap<>();

		    for (Object[] row : rows) {
		        String groupName = (String) row[0];
		        //DeviceForUserBean device = (DeviceForUserBean) row[1];
		        DeviceForUserBean device = new DeviceForUserBean(
		                ((Number) row[1]).longValue(),
		                (String) row[2]
		        );

		        result.computeIfAbsent(groupName, k -> new ArrayList<>()).add(device);
		    }

		    return result;
		}
		 
		 public Map<String, Object> getUserData(Users user) {

			    Map<String, Object> response = new HashMap<>();

			    if (user == null) {
			        return response;
			    }

			    CustomUserDTO dto = usersRepository.getUserData(user.getId());

			    if (dto == null) {
			        return response;
			    }

			    response.put("smsGatewayType", dto.getSmsGatewayType());
			    response.put("smsGatewayUrl", dto.getSmsGatewayUrl());

			    response.put("smtpHost", dto.getSmtpHost());
			    response.put("smtpUsername", dto.getSmtpUsername());
			    response.put("smtpPort", dto.getSmtpPort());
			    response.put("smtpEncryption", dto.getSmtpEncryption());

			    // ⚠️ Security: do NOT send actual password back
			    response.put("smtpPassword", null);

			    response.put("availableWidgets", dto.getAvailableWidgets());
			    response.put("dashboardMenu", dto.getDashboardMenu());
			    response.put("availablesubscriptionpoints", dto.getAvailable_subscription_points());
                 response.put("timezone", dto.getTimezone());
			    return response;
			}

		  public Map<Long, String> getAllUser(Users loggedUser) {

              List<Users> users = usersRepository.findAll();

              // ￼ Filter admin users first
              users = users.stream()
                              .filter(u -> u.getRoles() == null ||
                                              u.getRoles().stream()
                                                              .noneMatch(r -> "ROLE_ADMIN"
                                                                              .equalsIgnoreCase(r.getRoleName())))
                              .collect(Collectors.toList());

              // ￼ Convert to Map
              Map<Long, String> result = new LinkedHashMap<>();

              for (Users u : users) {
                      result.put(u.getId(), u.getUsername());
              }

              return result;
       }

		   public Users save(Users user) {
		        return usersRepository.save(user);
		    }

}
