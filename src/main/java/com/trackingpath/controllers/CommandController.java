package com.trackingpath.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.trackingpath.dtos.CommandDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.CommandService;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;

@RequestMapping("/commands")
@RestController
@RequiredArgsConstructor
public class CommandController {
	@Value("${command.server.url}")
	private String commandServerUrl;

	private final CommandService commandService;
	@Autowired
	AuthenticationService authenticationService;
	@Autowired
	private ActivityLogService activityLogService;

	// CREATE
	@PostMapping
	public ResponseEntity<?> addCommand(@RequestBody CommandDTO bean, HttpServletRequest request) {
		Users user = authenticationService.getCurrentUser();

		boolean saved = commandService.addCommand(bean);

		if (saved) {
			activityLogService.createActivity("NEW COMMAND CREATED",
					"COMMAND(" + bean.getCommandName() + ") CREATED BY " + user.getUsername(), user, request);
			return ResponseEntity.ok(Map.of("status", "success", "message", "Command added successfully"));
		}

		return ResponseEntity.status(400).body(Map.of("status", "error", "message", "Failed to add command"));
	}

	// READ ALL
	@GetMapping
	public ResponseEntity<Page<CommandDTO>> getAllCommands(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String search) {

		Pageable pageable = PageRequest.of(page, size);

		return ResponseEntity.ok(commandService.getAllCommands(search, pageable));
	}

	// READ BY ID
	@GetMapping("/{id}")
	public ResponseEntity<?> getCommandById(@PathVariable Long id) {

		CommandDTO dto = commandService.getCommandById(id);

		if (dto == null) {
			return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Command not found"));
		}

		return ResponseEntity.ok(dto);
	}

	// UPDATE
	@PutMapping
	public ResponseEntity<?> updateCommand(@RequestBody CommandDTO dto, HttpServletRequest request) {

		boolean updated = commandService.updateCommand(dto);
		Users user = authenticationService.getCurrentUser();

		if (updated) {
			activityLogService.createActivity("COMMAND UPDATED",
					"COMMAND(" + dto.getCommandName() + ") UPDATED BY " + user.getUsername(), user, request);
			return ResponseEntity.ok(Map.of("status", "success", "message", "Command updated successfully"));
		}

		return ResponseEntity.status(400).body(Map.of("status", "error", "message", "Failed to update command"));
	}

	// DELETE
	@DeleteMapping
	public ResponseEntity<?> deleteCommand(@RequestParam Long id, HttpServletRequest request) {
		Users user = authenticationService.getCurrentUser();
		boolean deleted = commandService.deleteCommand(id);

		if (deleted) {
			activityLogService.createActivity("COMMAND DELETED",
					"COMMAND DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(), user, request);
			return ResponseEntity.ok(Map.of("status", "success", "message", "Command deleted successfully"));
		}

		return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Command not found"));
	}

	// DOWNLOAD EXCEL TEMPLATE
	@GetMapping("/excel/template")
	public ResponseEntity<byte[]> downloadTemplate() throws IOException {

		byte[] fileBytes = commandService.downloadTemplate();

		return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=commandsUploadData.xlsx")
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(fileBytes);
	}

	// PREVIEW EXCEL DATA
	@PostMapping("/excel")
	public ResponseEntity<List<CommandDTO>> showExcel(@RequestParam("file") MultipartFile file) {
		try {
			List<CommandDTO> commands = commandService.parseExcel(file);
			return ResponseEntity.ok(commands);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(500).build();
		}
	}

	// UPLOAD & SAVE EXCEL DATA
	@PostMapping("/excel/save")
	public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {
		try {
			List<CommandDTO> uploadedData = commandService.parseAndSaveExcel(file);

			if (uploadedData.isEmpty()) {
				return ResponseEntity.badRequest().body("No data available to upload");
			}

			return ResponseEntity.ok("Data Uploaded Successfully: " + uploadedData.size() + " records");

		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("Failed to process the file");
		}
	}

	@GetMapping("/byDeviceId")
	public ResponseEntity<Map<String, String>> getCommandsByDeviceId(@RequestParam(name = "device_id") long deviceId) {

		Users user = authenticationService.getCurrentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).build();
		}

		Map<String, String> commands = commandService.getCommands(deviceId, user);
		return ResponseEntity.ok(commands);
	}

	@PostMapping("/send")
	public String sendCommands(@RequestBody CommandDTO commandBean) {

		Users user = authenticationService.getCurrentUser();
		String onlineStatus = commandService.getStatus(commandBean.getDeviceId());

		if (!"online".equalsIgnoreCase(onlineStatus)) {
			commandService.insertCommandLog(commandBean, user, "device is offline");
			return "Device is offline";
		}

		Map<String, Object> inner = new HashMap<>();
		inner.put("data", commandBean.getCommandCode());

		Map<String, Object> body = new HashMap<>();
		body.put("type", "custom");
		body.put("deviceId", commandBean.getDeviceId());
		body.put("attributes", inner);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(commandServerUrl, request, String.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				commandService.insertCommandLog(commandBean, user, "send successfully");
				return commandBean.getCommandName() + " sent successfully";
			} else {
				commandService.insertCommandLog(commandBean, user, "failed");
				return "Failed!";
			}
		} catch (Exception ex) {
			commandService.insertCommandLog(commandBean, user, ex.getMessage());
			return "Error while sending command";
		}
	}

	@GetMapping("/log")
	public ResponseEntity<Page<CommandDTO>> getCommandLogData(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String search) {

		Users user = authenticationService.getCurrentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).build();
		}

		Pageable pageable = PageRequest.of(page, size);

		return ResponseEntity.ok(commandService.getCommandLogData(user, search, pageable));
	}
}
