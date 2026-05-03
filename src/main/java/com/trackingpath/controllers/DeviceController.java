package com.trackingpath.controllers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trackingpath.dtos.DGMDTO;
import com.trackingpath.dtos.DeviceGroupDataDto;
import com.trackingpath.dtos.DeviceGroupDto;
import com.trackingpath.dtos.DeviceSettingDto;
import com.trackingpath.dtos.DevicesDetailsDto;
import com.trackingpath.dtos.DevicesUpdateDto;
import com.trackingpath.dtos.EventDataBean;
import com.trackingpath.dtos.HistoryDataPlaybackDTO;
import com.trackingpath.dtos.LiveDataBean;
import com.trackingpath.dtos.LiveDataDto;
import com.trackingpath.dtos.MaintenanceServiceDto;
import com.trackingpath.dtos.SensorListDTO;
import com.trackingpath.dtos.TodayActivityDTO;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.CommandService;
import com.trackingpath.services.DeviceService;
import com.trackingpath.services.DeviceSettingsService;
import com.trackingpath.services.LiveDataService;
import com.trackingpath.services.MaintenanceService;
import com.trackingpath.services.SensorService;
import com.trackingpath.services.UserService;
import com.trackingpath.util.DateTimeHelper;
import com.trackingpath.util.DateTimeUtil;

import lombok.RequiredArgsConstructor;

import com.trackingpath.entities.GroupEntity;
import com.trackingpath.entities.Users;

@RequestMapping("/devices")
@RestController
@RequiredArgsConstructor

public class DeviceController {

	@Autowired
	AuthenticationService authenticationService;

	private final DeviceSettingsService deviceSettingsService;

	private final LiveDataService liveDataService;
	private final DeviceService deviceService;
	private final UserService userService;
	private final CommandService commandService;
	private final SensorService sensorService;
	private final MaintenanceService maintenanceService;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    
    @Value("${FILE_UPLOAD_PATH}")
	private String FILE_UPLOAD_PATH;
    
    private String saveFile(MultipartFile file) throws IOException {

        // 1️⃣ Optional file check
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 2️⃣ Get original filename
        String originalName = file.getOriginalFilename();

        if (originalName == null || !originalName.contains(".")) {
            return null;
        }

        // 3️⃣ Generate unique name
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + ext;

        // 4️⃣ Save file
        File folder = new File(FILE_UPLOAD_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File destination = new File(folder, newFileName);
        file.transferTo(destination);

        return newFileName;
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createDevice(
            @RequestPart("dto") String dtoJson,
            @RequestPart(value = "rcFile", required = false) MultipartFile rcFile,
            @RequestPart(value = "insuranceFile", required = false) MultipartFile insuranceFile) {

        try {
            // ✅ STEP 1: Convert JSON → DTO
        	ObjectMapper objectMapper = new ObjectMapper();
        	objectMapper.registerModule(new JavaTimeModule());
            DeviceSettingDto dto = objectMapper.readValue(dtoJson, DeviceSettingDto.class);

            Users user = authenticationService.getCurrentUser();

            boolean isAdmin = user.getRoles() != null &&
                    user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getRoleName()));

            if (!isAdmin) {
                int availablePoint = user.getAvailablesubscriptionpoints();
                if (availablePoint <= 0) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("Insufficient subscription balance!");
                }

                user.setAvailablesubscriptionpoints(availablePoint - 1);
                userService.save(user);
            }

            if (dto.getGroupId() == null) {
                dto.setGroupId(1L);
            }

            // ✅ STEP 2: Handle files

            if (rcFile != null && !rcFile.isEmpty()) {
                String rcPath = saveFile(rcFile);
                dto.setRcPath(rcPath);
            }

            if (insuranceFile != null && !insuranceFile.isEmpty()) {
                String insurancePath = saveFile(insuranceFile);
                dto.setInsurancePath(insurancePath);
            }

            Long deviceId = deviceSettingsService.createDevice(dto, user);

            return ResponseEntity.ok("Device created successfully with ID: " + deviceId);

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 very important
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating device: " + e.getMessage());
        }
    }
	@GetMapping("/devices/timezone")
	public ResponseEntity<List<DeviceGroupDto>> getDeviceGroup() {
		Users user = authenticationService.getCurrentUser();

		return ResponseEntity.ok(deviceSettingsService.getDeviceGroupData(user.getId()));
	}

	@GetMapping(value = "/live/devices")
	public ResponseEntity<List<LiveDataDto>> getLiveData(
			@RequestParam(name = "date", required = false, defaultValue = "") String date) {
		Users user = authenticationService.getCurrentUser();

		String stime;
		String etime;

		if (date == null || date.isEmpty()) {
			LocalDate todayDate = LocalDate.now();
			date = String.valueOf(todayDate); // Set date to today's date
		}
		// Convert user-provided or default date to start and end times in UTC
		stime = DateTimeHelper.localToIst(date + " 00:00:00", "UTC");
		etime = DateTimeHelper.localToIst(date + " 23:59:59", "UTC");
		System.out.println("livestime"+stime);
		List<LiveDataDto> liveData = liveDataService.getLiveData(user, stime, etime);

		return ResponseEntity.ok(liveData);
	}

	@GetMapping("/id-name-map")
	public ResponseEntity<Map<Long, String>> getDeviceIdNameMap() {
		Users user = authenticationService.getCurrentUser();
		return ResponseEntity.ok(deviceService.getDeviceIdNameMap(user.getId()));
	}

	@GetMapping("/list")
	public ResponseEntity<Map<Long, String>> listDevices() {
		Users user = authenticationService.getCurrentUser();

		Map<Long, String> devices = deviceService.getDevicesForUser(user);

		return ResponseEntity.ok(devices);
	}
	@GetMapping("/PlaybackData")
	public ResponseEntity<HistoryDataPlaybackDTO> getPlaybackHistoryData(
	        @RequestParam String start_time,
	        @RequestParam String end_time,
	        @RequestParam long deviceId,
	        @RequestParam(defaultValue = "0") long time_interval,
	        @RequestParam(defaultValue = "IDLING", required = false) String type) {

	    Users user = authenticationService.getCurrentUser();

	    String deviceTimezone = deviceService.findTimezoneByDeviceId(deviceId);


	    LocalDateTime userStartTime = LocalDateTime.parse(start_time, formatter);
	    LocalDateTime userEndTime = LocalDateTime.parse(end_time, formatter);

	    LocalDateTime deviceStartTime = DateTimeHelper.convertUserZoneToDeviceZone(
	            userStartTime,
	            user.getTimezone(),
	            deviceTimezone
	    );

	    LocalDateTime deviceEndTime = DateTimeHelper.convertUserZoneToDeviceZone(
	            userEndTime,
	            user.getTimezone(),
	            deviceTimezone
	    );

	    String startTime = deviceStartTime.format(formatter);
	    String endTime = deviceEndTime.format(formatter);

	    HistoryDataPlaybackDTO hd = deviceService.getPlaybackData(
	            deviceId,
	            startTime,
	            endTime,
	            user,
	            time_interval,
	            type
	    );

	    return ResponseEntity.ok(hd);
	}


	@GetMapping("/{deviceId}")
	public DevicesDetailsDto getDeviceByIdForUpdate(@PathVariable Long deviceId) {

		Users user = authenticationService.getCurrentUser();

		DevicesUpdateDto deviceData = deviceService.getDeviceByIdForUpdate(deviceId, user);
		LiveDataBean liveData = liveDataService.findLiveDataByDeviceId(deviceId, user);

		Map<Long, String> groupNames = deviceSettingsService.getAllGroupName(user);
		Map<Long, String> userMap = userService.getAllUsers(user);
		List<String> deviceModels = commandService.getDeviceModels(user);
		List<SensorListDTO> sensorData = sensorService.getSensorData(user, deviceId);
		List<MaintenanceServiceDto> maintenanceData = maintenanceService.getMaintenanceMultipleDataById(user, deviceId);

		return DevicesDetailsDto.builder().deviceData(deviceData).liveData(liveData).groupNames(groupNames)
				.userMap(userMap).deviceModels(deviceModels).sensorData(sensorData).maintenanceData(maintenanceData)
				.build();
	}

	@PutMapping(value = "/{deviceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> updateDevice(
		    @PathVariable Long deviceId,

		    @ParameterObject
		    @ModelAttribute DevicesUpdateDto request,   

		    @RequestParam(value = "rcFile", required = false) MultipartFile rcFile,
		    @RequestParam(value = "insuranceFile", required = false) MultipartFile insuranceFile,
		    @AuthenticationPrincipal Users user
		) {

		// Ensure path ID is used
		request.setDeviceId(deviceId);
		
		try {


		    if (rcFile != null && !rcFile.isEmpty()) {
		        String rcPath = saveFile(rcFile);
		        request.setRcPath(rcPath);
		    }

		    if (insuranceFile != null && !insuranceFile.isEmpty()) {
		        String insurancePath = saveFile(insuranceFile);
		        request.setInsurancePath(insurancePath);
		    }

		} catch (IOException e) {
		    e.printStackTrace();
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .body("File upload failed");
		}

		boolean updated = deviceService.updateDevicesData(request, user);

		if (updated) {
			return ResponseEntity.ok(Map.of("success", true, "message", "Device updated successfully"));
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("success", false, "message", "Device update failed"));
	}
	@GetMapping(value = "/livedataforfollow")
	public ResponseEntity<LiveDataDto> getLiveDataByDevice(
	        @RequestParam("deviceId") Long deviceId,
	        @RequestParam(name = "date", required = false, defaultValue = "") String date) {

	    Users user = authenticationService.getCurrentUser();

	    if (date == null || date.isEmpty()) {
	        date = LocalDate.now().toString();
	    }

	    String stime = DateTimeHelper.localToIst(date + " 00:00:00", "UTC");
	    String etime = DateTimeHelper.localToIst(date + " 23:59:59", "UTC");

	    LiveDataDto liveData =
	            liveDataService.getLiveDataByDevice(user, deviceId, stime, etime);

	    return ResponseEntity.ok(liveData);
	}
	
    @GetMapping("/groups")
    public Map<Long, String> getAllGroupNames() {
		Users user = authenticationService.getCurrentUser();
        return deviceSettingsService.getAllGroupName(user);
    }
    
    
    
    
    
    @PostMapping(value = "/DeviceGroupData", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addDeviceGroupData(@RequestBody DGMDTO bean) {
        try {
            Users user = authenticationService.getCurrentUser();

            // 1️⃣ Add group and get the entity
            GroupEntity group = deviceSettingsService.addDataIntoGroup(bean, user);
            // Note: service now returns GroupEntity instead of just ID

            if (group != null && group.getId() != null) {

                // 2️⃣ Add devices to group mapping
                deviceSettingsService.addDataInDGM(bean, user, group);

                // 3️⃣ Remove ungrouped devices
                deviceSettingsService.removeUngroupedDevicesFromDGM(bean);

                return ResponseEntity.ok("Devices mapped and group created successfully!");
            } else {
                return ResponseEntity.badRequest().body("Failed to create group.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    
	@GetMapping("/getDeviceGroupDataForUpdate")
	public List<DeviceGroupDataDto> getDeviceGroupDataForUpdate(
			@RequestParam(name = "group_name", defaultValue = "") String group_name,
			@RequestParam(name = "deviceIds", defaultValue = "") String deviceIds) {
        Users user = authenticationService.getCurrentUser();
		return deviceSettingsService.getDeviceGroupDataForUpdate(group_name, deviceIds, user);
	}
    
	
	/*
	 * 
	 * @PutMapping(value = "/DeviceGroupData",consumes =
	 * MediaType.APPLICATION_JSON_VALUE) public ResponseEntity<String>
	 * updateDeviceGroupData(@RequestBody DGMDTO bean) { Users user =
	 * authenticationService.getCurrentUser();
	 * 
	 * deviceSettingsService.deletePreviousGroupDevices(bean.getGroup_id(), user);
	 * // delete data from dgm
	 * deviceSettingsService.updateGroupName(bean.getGroup_name(),
	 * bean.getGroup_id(), user);
	 * 
	 * 
	 * int[] insertLenght =
	 * deviceSettingsService.insertAfterDeleteGroupMapping(bean, user);
	 * 
	 * deviceSettingsService.deleteFromGroups(user);
	 * 
	 * deviceSettingsService.removeUngroupedDevicesFromDGM(bean, user);
	 * 
	 * deviceSettingsService.addUngroupedDevices(user);
	 * 
	 * 
	 * if (insertLenght.length > 0) {
	 * 
	 * return ResponseEntity.ok("Devices Mapped and Group updated  successfully!");
	 * } return null;
	 * 
	 * }
	 */
	

	@PostMapping("/getallobject")
	public Map<String, Object> getallobject(@RequestBody Map<String, Object> requestData) {

	    Users user = authenticationService.getCurrentUser();

	    int draw = ((Number) requestData.get("draw")).intValue();
	    int start = ((Number) requestData.get("start")).intValue();
	    int length = ((Number) requestData.get("length")).intValue();

	    String search = requestData.get("search") != null 
	            ? requestData.get("search").toString() 
	            : "";

	    return deviceSettingsService.getAlldataobject(user, draw, start, length, search);
	}

    /* -------------------- USERS LIST -------------------- */
    @GetMapping("/users")
    public Map<Long, String> getAllUsers() {
		Users user = authenticationService.getCurrentUser();
        return userService.getAllUsers(user);
    }
    @GetMapping(value = "/livedataByDeviceId")
	public ResponseEntity<LiveDataBean> livedataByDeviceId(
	        @RequestParam("deviceId") Long deviceId) {

	    Users user = authenticationService.getCurrentUser();

	    LiveDataBean liveData = liveDataService.findLiveDataByDeviceId(deviceId, user);
	    return ResponseEntity.ok(liveData);
	}
    
    @GetMapping("/PlaybackDataForDriving")
	public List<EventDataBean> getPlaybackDataForDriving(
	        @RequestParam String start_time,
	        @RequestParam String end_time,
	        @RequestParam Long deviceId
	) {

	    Users user = authenticationService.getCurrentUser();

	    String startTime = formatDate(start_time);
	    String endTime = formatDate(end_time);

	    return deviceService.getPlaybackDrivingData(
	            deviceId,
	            startTime,
	            endTime,
	            user
	    );
	}
	public static String formatDate(String inputDate) {

        DateTimeFormatter inputFormatter =
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        DateTimeFormatter outputFormatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            LocalDateTime dateTime =
                    LocalDateTime.parse(inputDate, inputFormatter);

            return dateTime.format(outputFormatter);

        } catch (DateTimeParseException e) {
            System.err.println("Failed to parse input date: " + inputDate);
            return null;
        }
    }
	
	@PutMapping("/updateDeviceGroupData")
	public ResponseEntity<String> updateDeviceGroupData2(@RequestBody DGMDTO bean) {
		Users user = authenticationService.getCurrentUser();
	    deviceSettingsService.updateDeviceGroupData(bean);
	    deviceSettingsService.updateGroupName(bean.getGroup_name(), bean.getGroup_id(), user);

	    return ResponseEntity.ok("Device group updated successfully");
	}
	
	@GetMapping("/todayactivity")
	public ResponseEntity<TodayActivityDTO> getTodayactivity(
	        @RequestParam("deviceId") Long deviceId,
	        @RequestParam(name = "date", required = false, defaultValue = "") String date) {

	    Users user = authenticationService.getCurrentUser();
	    ZoneId userZone = ZoneId.of(user.getTimezone());
	   
	    if (date == null || date.isBlank()) {
	        date = LocalDate.now(userZone).toString();

	    }
	    ZonedDateTime userStart = LocalDate.parse(date).atStartOfDay(userZone);
	    ZonedDateTime userEnd = LocalDate.parse(date).atTime(23, 59, 59).atZone(userZone);

	   
	    LocalDateTime stime = userStart.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
	    LocalDateTime etime = userEnd.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();


	    TodayActivityDTO activityData =
	            deviceService.getTodayactivity(user, deviceId, stime, etime);

	    return ResponseEntity.ok(activityData);
	}
	@GetMapping("/list/notAssignAnyDriver")
	public ResponseEntity<Map<Long, String>> notAssignAnyDriver() {
		Users user = authenticationService.getCurrentUser();

		Map<Long, String> devices = deviceService.notAssignAnyDriver(user);

		return ResponseEntity.ok(devices);
	}

}
