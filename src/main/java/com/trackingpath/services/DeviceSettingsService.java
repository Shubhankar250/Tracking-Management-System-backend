package com.trackingpath.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackingpath.dtos.DGMDTO;
import com.trackingpath.dtos.DeviceGroupDataDto;
import com.trackingpath.dtos.DeviceGroupDto;
import com.trackingpath.dtos.DeviceSettingDto;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.DeviceGroupMapping;
import com.trackingpath.entities.DevicesUserMapping;
import com.trackingpath.entities.GroupEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.exceptions.GlobalExceptionHandler;
import com.trackingpath.mapper.DeviceGroupMappingMapper;
import com.trackingpath.mapper.DeviceMapper;
import com.trackingpath.mapper.GroupMapper;
import com.trackingpath.repositories.DeviceGroupMappingRepository;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.repositories.DeviceTimezoneView;
import com.trackingpath.repositories.DevicesUserMappingRepository;
import com.trackingpath.repositories.GroupRepository;
import com.trackingpath.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceSettingsService {

	private final GlobalExceptionHandler globalExceptionHandler;

	private final DeviceRepository deviceRepository;
	private final DeviceGroupMappingRepository deviceGroupMappingRepository;
	private final DeviceMapper deviceMapper;
	private final GroupRepository groupRepository;
	@Autowired
	private DevicesUserMappingRepository devicesUserMappingRepository;
	@Autowired
	AuthenticationService authenticationService;
	@Autowired
	private UserRepository usersRepository;
	public Long createDevice(DeviceSettingDto dto, Users currentUser) {

	    // 1️⃣ Map DTO → Entity
	    DeviceEntity device = deviceMapper.toEntity(dto, currentUser);
	    
	    device.setRcPath(dto.getRcPath());
	    device.setInsurancePath(dto.getInsurancePath());

	    // 2️⃣ Save device
	    DeviceEntity savedDevice = deviceRepository.save(device);

	    // ---------------- Default Values ----------------
	    Long groupId = (dto.getGroupId() != null && dto.getGroupId() > 0)
	            ? dto.getGroupId()
	            : 1L; // default group

	    Long userId = (dto.getUserId() != null && dto.getUserId() > 0)
	            ? dto.getUserId()
	            : currentUser.getId(); // default user

	    // 3️⃣ Fetch group
	    GroupEntity group = groupRepository.findById(groupId)
	            .orElseThrow(() -> new RuntimeException("Group not found"));

	    // 4️⃣ Create DeviceGroupMapping
	    DeviceGroupMapping mapping = DeviceGroupMapping.builder()
	            .device(savedDevice)
	            .group(group)
	            .userId(userId)
	            .adminId(currentUser.getId())
	            .creationTime(LocalDateTime.now())
	            .build();

	    deviceGroupMappingRepository.save(mapping);

	    // 5️⃣ Create DevicesUserMapping
	    Users assignedUser = usersRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    DevicesUserMapping userMapping = DevicesUserMapping.builder()
	            .device(savedDevice)
	            .user(assignedUser)
	            .admin(currentUser)   // entity expects Users object
	            .build();

	    devicesUserMappingRepository.save(userMapping);

	    return savedDevice.getId();
	}
	public List<DeviceGroupDto> getDeviceGroupData(Long userId) {
		List<DeviceGroupMapping> mappings = deviceGroupMappingRepository.findByDevice_UserId(userId);

		List<DeviceGroupDto> result = new ArrayList<>();

		for (DeviceGroupMapping dgm : mappings) {

			DeviceEntity d = dgm.getDevice();
			GroupEntity g = dgm.getGroup();

			DeviceGroupDto dto = new DeviceGroupDto();
			dto.setDeviceId(d.getId());
			dto.setName(d.getName());
			dto.setUniqueid(d.getUniqueid());
			dto.setPlateNumber(d.getPlateNumber());
			dto.setVehicleStatus(d.getVehicleStatus());
			dto.setDeviceModel(d.getDeviceModel());

			if (g != null) {
				dto.setGroupId(g.getId());
				dto.setGroupName(g.getName());
			} else {
				dto.setGroupName("Ungrouped");
			}

			result.add(dto);
		}

		return result;
	}

	public Map<Integer, String> getDeviceTimezone(Users user) {

		Map<Integer, String> result = new HashMap<>();

		List<DeviceTimezoneView> list = deviceRepository.findAllDeviceTimezones();
		for (DeviceTimezoneView v : list) {
			result.put(v.getId(), v.getDevicetimezone());
		}
		return result;
	}


	public Map<Long, String> getAllGroupName(Users user) {

		List<GroupEntity> groups;

		if (user.getRoles() != null || user.getRoles().stream().noneMatch(r -> "ROLE_ADMIN".equals(r.getRoleName()))) {
			groups = groupRepository.findByAdminUser(user.getId());
		} else {
			List<Long> deviceIds = Arrays.stream(user.getAssign_device_ids().split(",")).map(String::trim)
					.map(Long::valueOf).toList();

			groups = groupRepository.findGroupsByDeviceIds(deviceIds);
		}

		Map<Long, String> result = new LinkedHashMap<>();
		for (GroupEntity g : groups) {
			result.put(g.getId().longValue(), g.getName());
		}

		return result;
	}

    public GroupEntity addDataIntoGroup(DGMDTO bean, Users user) {
        if (bean == null || user == null) return null;

        GroupEntity group = GroupMapper.toEntity(bean, user, user); 
        return groupRepository.saveAndFlush(group);
    }



	   /**
	     * Add devices to a group
	     */
	    public boolean addDataInDGM(DGMDTO bean, Users user, GroupEntity group) {
	        if (bean == null || user == null || group == null || bean.getDeviceIds() == null) return false;
	        deviceGroupMappingRepository.deleteDevicesFromOtherGroups(bean.getDeviceIds(), group.getId());

	        List<DeviceGroupMapping> mappings = bean.getDeviceIds().stream()
	                .map(deviceId -> DeviceGroupMappingMapper.toEntity(deviceId, group, user, user))
	                .filter(java.util.Objects::nonNull)
	                .toList();

	        deviceGroupMappingRepository.saveAll(mappings);
	        return true;
	    }

	    /**
	     * Remove devices from ungrouped group
	     */
	    public void removeUngroupedDevicesFromDGM(DGMDTO bean) {
	        if (bean == null || bean.getDeviceIds() == null || bean.getDeviceIds().isEmpty()) return;

	        deviceGroupMappingRepository.deleteUngroupedDevices(bean.getDeviceIds());
	    }
	    

	    public List<DeviceGroupDataDto> getDeviceGroupDataForUpdate(String groupName,String deviceIds,Users user) {
        
	    	List<Long> deviceIdList = Arrays.stream(deviceIds.split(","))
	                .map(String::trim)
	                .map(Long::valueOf)
	                .toList();

	        return deviceGroupMappingRepository.getDeviceGroupDataForUpdate(groupName, deviceIdList,user.getAdminId()
	        );
	    }

		public int deletePreviousGroupDevices(long group_id, Users user) {
			// TODO Auto-generated method stub
			return groupRepository.deletePreviousGroupDevices(group_id, user.getId());
		}

		public int updateGroupName(String group_name, long group_id, Users user) {
			return groupRepository.updateGroupName(group_name, group_id, user.getId());
			
		}

		public void deleteFromGroups(Users user) {
			deviceGroupMappingRepository.deleteUnusedGroups(user.getId());
			
		}

		public boolean removeUngroupedDevicesFromDGM(DGMDTO bean, Users user) {
		    List<Long> deviceIds = bean.getDeviceIds();
		    if (deviceIds == null || deviceIds.isEmpty()) {
		        return false;
		    }
		    int rowsDeleted = deviceGroupMappingRepository.deleteUngroupedDevices(deviceIds);
		    return rowsDeleted > 0;
		}

		public int[] insertAfterDeleteGroupMapping(DGMDTO bean, Users user) {
		    List<DeviceGroupMapping> entities = DeviceGroupMappingMapper.toEntities(bean, user);
		    deviceGroupMappingRepository.saveAll(entities);

		    int[] result = new int[entities.size()];
		    for (int i = 0; i < entities.size(); i++) {
		        result[i] = 1;
		    }
		    return result;
		}

		@Transactional
		public int[] addUngroupedDevices(Users user) {

		    List<Long> deviceIds = deviceRepository.findDeviceIdsByUser(user.getId());
		    if (deviceIds.isEmpty()) {
		        return new int[0];
		    }

		    GroupEntity group = groupRepository.findByNameAndAdminId("Ungrouped", user.getAdminId())
		            .orElseThrow(() -> new RuntimeException("Ungrouped group not found"));

		    List<DeviceGroupMapping> mappings = new ArrayList<>();

		    for (Long deviceId : deviceIds) {
		        DeviceGroupMapping dgm = DeviceGroupMapping.builder()
		                .device(DeviceEntity.builder().id(deviceId).build())
		                .group(group)   // ✅ use entity directly
		                .userId(user.getId())
		                .adminId(user.getAdminId())
		                .creationTime(LocalDateTime.now())
		                .build();

		        mappings.add(dgm);
		    }
		    
		    deviceGroupMappingRepository.saveAll(mappings);

		    int[] result = new int[mappings.size()];
		    Arrays.fill(result, 1);
		    return result;
		}

		public Map<String, Object> getAlldataobject(Users user, int draw, int start, int length, String search) {
			return deviceRepository.getAlldataobject(user, draw, start, length,search);

		}

		 
		@Transactional
		public void updateDeviceGroupData(DGMDTO bean) {

			Users user = authenticationService.getCurrentUser();

			GroupEntity targetGroup = groupRepository.findById(bean.getGroup_id())
					.orElseThrow(() -> new RuntimeException("Group not found"));

			GroupEntity ungrouped = groupRepository.findByNameAndAdminId("Ungrouped", user.getAdminId())
					.orElseThrow(() -> new RuntimeException("Ungrouped not found"));

			// Remove duplicates from payload
			Set<Long> payloadDeviceIds = new HashSet<>(bean.getDeviceIds());

			// ✅ Fetch ALL mappings for this user (IMPORTANT FIX)
			List<DeviceGroupMapping> allMappings = deviceGroupMappingRepository.findAllMappingsByUser(user.getId(),
					user.getAdminId());

			// Map<deviceId, mapping>
			Map<Long, DeviceGroupMapping> deviceMap = allMappings.stream()
					.collect(Collectors.toMap(m -> m.getDevice().getId(), m -> m, (a, b) -> a // safety in case of
																								// duplicates
					));

			List<DeviceGroupMapping> updates = new ArrayList<>();
			List<DeviceGroupMapping> inserts = new ArrayList<>();

			// =========================
			// 1️⃣ HANDLE SELECTED DEVICES
			// =========================
			for (Long deviceId : payloadDeviceIds) {

				DeviceGroupMapping existing = deviceMap.get(deviceId);

				if (existing != null) {
					// ✅ UPDATE existing mapping (MAIN FIX)
					if (!existing.getGroup().getId().equals(targetGroup.getId())) {
						existing.setGroup(targetGroup);
						existing.setCreationTime(LocalDateTime.now());
						updates.add(existing);
					}
				} else {
					// ✅ INSERT only if mapping does not exist
					DeviceGroupMapping mapping = DeviceGroupMapping.builder()
							.device(DeviceEntity.builder().id(deviceId).build()).group(targetGroup).userId(user.getId())
							.adminId(user.getAdminId()).creationTime(LocalDateTime.now()).build();

					inserts.add(mapping);
				}
			}

			// =========================
			// 2️⃣ HANDLE REMOVED DEVICES
			// =========================
			for (DeviceGroupMapping mapping : allMappings) {

				boolean isSameGroup = mapping.getGroup().getId().equals(bean.getGroup_id());
				boolean notInPayload = !payloadDeviceIds.contains(mapping.getDevice().getId());

				if (isSameGroup && notInPayload) {
					// move to Ungrouped
					mapping.setGroup(ungrouped);
					mapping.setCreationTime(LocalDateTime.now());
					updates.add(mapping);
				}
			}

			// =========================
			// 3️⃣ SAVE CHANGES
			// =========================
			if (!updates.isEmpty()) {
				deviceGroupMappingRepository.saveAll(updates);
			}

			if (!inserts.isEmpty()) {
				deviceGroupMappingRepository.saveAll(inserts);
			}
		} 
		 
		 
		 
		 
		 
		 
		 
		}
		



	
	    
	    
	    
	    

