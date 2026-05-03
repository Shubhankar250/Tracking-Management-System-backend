package com.trackingpath.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.DeviceGroupDataProjection;
import com.trackingpath.dtos.DeviceModalDTO;
import com.trackingpath.dtos.DeviceModalSearchDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.exceptions.FileSizeExceededException;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.DeviceModalService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/devicemodal")
public class DeviceModalController {

	@Autowired
	private DeviceModalService deviceModalService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private ActivityLogService activityLogService;

	@Value("${FILE_UPLOAD_PATH}")
	private String FILE_UPLOAD_PATH;
	
	@Value("${IMAGE_MAX_SIZE}")
	private long IMAGE_MAX_SIZE;
	
	@Value("${MANUAL_MAX_SIZE}")
	private long MANUAL_MAX_SIZE;


	@GetMapping("")
	public ResponseEntity<Map<String, Object>> getAllDeviceModals(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String search) {

		Users user = authenticationService.getUser();

		Map<String, Object> response = deviceModalService.getAllDeviceModals(user, page, size, search);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	public DeviceModalDTO getById(@PathVariable Long id) {
	
		return deviceModalService.getById(id);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> insertDeviceModal(@RequestPart("data") String data,
			@RequestPart(value = "image", required = false) MultipartFile image,
			@RequestPart(value = "userManual", required = false) MultipartFile userManual,
			@RequestPart(value = "protocolManual", required = false) MultipartFile protocolManual,HttpServletRequest request)
			throws IOException {

		Users user = authenticationService.getUser();

		ObjectMapper mapper = new ObjectMapper();
		DeviceModalDTO bean = mapper.readValue(data, DeviceModalDTO.class);
		System.out.println("File Size :- "+image.getSize()+" MaxSize :- "+IMAGE_MAX_SIZE);
		if (image != null && !image.isEmpty()) {
			bean.setImage(saveFile(image, IMAGE_MAX_SIZE, "image"));
		}

		if (userManual != null && !userManual.isEmpty()) {
			bean.setUserManual(saveFile(userManual, MANUAL_MAX_SIZE, "userManual"));
		}

		if (protocolManual != null && !protocolManual.isEmpty()) {
			bean.setProtocolManual(saveFile(protocolManual, MANUAL_MAX_SIZE, "protocolManual"));
		}

		boolean status = deviceModalService.insertDeviceModal(bean, user);

		if (status) {
			activityLogService.createActivity("DEVICE MODAL CREATED",
					"DEVICE MODAL(" + bean.getModalName() + ") CREATED BY " + user.getUsername(), user, request);
		}

		return ResponseEntity.ok(status ? "Added Successfully" : "Failed!");
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> updateDeviceModal(@PathVariable Long id,
			@RequestPart(value = "data", required = false) String data,
			@RequestPart(value = "image", required = false) MultipartFile image,
			@RequestPart(value = "userManual", required = false) MultipartFile userManual,
			@RequestPart(value = "protocolManual", required = false) MultipartFile protocolManual,
			HttpServletRequest request)
			throws IOException {

		Users user = authenticationService.getUser();

		DeviceModalDTO dto = new DeviceModalDTO();

		if (data != null && !data.isEmpty()) {
			ObjectMapper mapper = new ObjectMapper();
			dto = mapper.readValue(data, DeviceModalDTO.class);
		}

		if (image != null && !image.isEmpty()) {
			dto.setImage(saveFile(image, IMAGE_MAX_SIZE, "image"));
		}

		if (userManual != null && !userManual.isEmpty()) {
			dto.setUserManual(saveFile(userManual, MANUAL_MAX_SIZE, "userManual"));
		}

		if (protocolManual != null && !protocolManual.isEmpty()) {
			dto.setProtocolManual(saveFile(protocolManual, MANUAL_MAX_SIZE, "protocolManual"));
		}

		boolean status = deviceModalService.updateDeviceModal(id, dto, user);

		if (status) {
			activityLogService.createActivity("DEVICE MODAL UPDATED", "DEVICE MODAL UPDATED BY " + user.getUsername(),
					user, request);
		}

		return ResponseEntity.ok(status ? "Device Modal updated successfully" : "Update failed");
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteDeviceModal(@PathVariable Long id, HttpServletRequest request) {

		Users user = authenticationService.getUser();
		boolean status = deviceModalService.deleteDeviceModal(id, user);
		if (status) {
			activityLogService.createActivity("DEVICE MODAL DELETED",
					"DEVICE MODAL DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(), user, request);
		}

		return ResponseEntity.ok(status ? "Deleted successfully" : "Delete failed");
	}

	private String saveFile(MultipartFile file, long maxSize, String fieldName) throws IOException {

		 if (file.getSize() > maxSize) {
		        throw new FileSizeExceededException(
		                fieldName,
		                fieldName + " exceeds allowed size limit"
		        );
		    }

		String fileName = file.getOriginalFilename();
		if (fileName == null || !fileName.contains("."))
			return null;

		String ext = fileName.substring(fileName.lastIndexOf("."));
		String newName = UUID.randomUUID() + ext;

		File dest = new File(FILE_UPLOAD_PATH, newName);
		dest.getParentFile().mkdirs();
		file.transferTo(dest);

		return newName;
	}
	 @GetMapping("/names")
     public List<String> getAllModalNames() {
             return deviceModalService.getAllModalNames();
     }

	 @GetMapping("/search")
	 public ResponseEntity<List<DeviceModalSearchDTO>> searchDeviceModal(
	         @RequestParam String keyword) {


	     List<DeviceModalSearchDTO> list =
	             deviceModalService.searchDeviceModal(keyword);

	     return ResponseEntity.ok(list);
	 }
	 @GetMapping("/allCompanyNames")
     public List<String> getAllCompanyNames(){
         return deviceModalService.getAllCompanyNames();
     }
     
     @GetMapping("/allDevicesByCompanyNames")
     public ResponseEntity<List<DeviceModalDTO>> getDevicesByCompany(
    		 @RequestParam(required = false) String companyName) {

             List<DeviceModalDTO> devices = deviceModalService.getDevicesByCompanyName(companyName);
             return ResponseEntity.ok(devices);
     }
	
     @PostMapping("/AlertTypeForADASandDMS")
     public List<DeviceGroupDataProjection> getGroupedDevices(@RequestBody List<Long> deviceIds) {
             return deviceModalService.getGroupedDevices(deviceIds);
     }
     
     
}
