package com.trackingpath.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.dtos.CustomUserDTO;
import com.trackingpath.dtos.DriverSetupBean;
import com.trackingpath.dtos.SetupTemplateBean;
import com.trackingpath.entities.Users;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.DeviceService;
import com.trackingpath.services.SetupService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/setup")
public class SetUpController {

	@Autowired
	private SetupService setupService;

	@Autowired
	DeviceService deviceService;

	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private ActivityLogService activityLogService;


	// CREATE
	@PostMapping(value = "/adddriverData", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> addDriver(@RequestBody DriverSetupBean bean,HttpServletRequest request) {

		Users user = authenticationService.getUser();

		boolean status = setupService.adddriverData(bean, user);
		if(status) {
			activityLogService.createActivity("DRIVER SETUP CREATED",
			"DRIVER SETUP(" + bean.getName() + ") CREATED BY " + user.getUsername(), user, request);
		}

		return ResponseEntity.ok(status ? "Added Successfully" : "Failed!");
	}

	// READ
	@GetMapping("/getdriverdata")
	public ResponseEntity<Map<String, Object>> getDriverData(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String search) {
		Users user = authenticationService.getUser();
		Map<String, Object> response = setupService.getDriverData(user, page, size, search);
		return ResponseEntity.ok(response);
	}

	@PutMapping(value = "/updatedriverData", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updateDriverData(@RequestBody DriverSetupBean bean,HttpServletRequest request) {

		Users user = authenticationService.getUser();
		boolean status = setupService.updateDriverData(bean, user);
		if(status) {
			activityLogService.createActivity("DRIVER SETUP UPDATED",
				"DRIVER SETUP(" + bean.getName() + ") UPDATED BY " + user.getUsername(), user, request);
		}
		return ResponseEntity.ok(status ? "Driver Updated Successfully" : "Failed!");
	}

	@DeleteMapping("/deletedriver")
	public ResponseEntity<String> deleteDriver(@RequestParam("id") long id,HttpServletRequest request) {

		Users user = authenticationService.getUser();

		boolean status = setupService.deleteDriver(id, user);
		if(status) {
			activityLogService.createActivity("DRIVER SETUP DELETED",
				"DRIVER SETUP DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(), user, request);	
		}

		return ResponseEntity.ok(status ? "Driver deleted successfully" : "Failed!");
	}

	@GetMapping("/getSMSdata")
	public ResponseEntity<Map<String, Object>> getSMSData(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String search) {
		Users user = authenticationService.getUser();
		Map<String, Object> response = setupService.getSmsData(user, page, size, search);
		return ResponseEntity.ok(response);
	}

	// CREATE
	@PostMapping(value = "/addSMSData", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> addSMSData(@RequestBody SetupTemplateBean bean,HttpServletRequest request) {

		Users user = authenticationService.getUser();

		boolean status = setupService.addsmsData(bean, user);
		if(status) {
			activityLogService.createActivity("SMS SETUP CREATED",
				"SMS SETUP(" + bean.getTemplateName() + ") CREATED BY " + user.getUsername(), user, request);
		}
		return ResponseEntity.ok(status ? "Added Successfully" : "Failed!");
	}

	@PutMapping(value = "/updateSMSData", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updateSMSData(@RequestBody SetupTemplateBean bean,HttpServletRequest request) {

		Users user = authenticationService.getUser();
		boolean status = setupService.updatesmsData(bean, user);
		if(status) {
			activityLogService.createActivity("SMS SETUP UPDATED",
				"SMS SETUP(" + bean.getTemplateName() + ") UPDATED BY " + user.getUsername(), user, request);
	}
		return ResponseEntity.ok(status ? "SMS Updated Successfully" : "Failed!");
	}

	@GetMapping("/getEMAILdata")
	public ResponseEntity<Map<String, Object>> getEMAILData(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String search) {
		Users user = authenticationService.getUser();
		Map<String, Object> response = setupService.getEmailData(user, page, size, search);
		return ResponseEntity.ok(response);
	}

	// CREATE
	@PostMapping(value = "/addEMAILData", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> addEMAILData(@RequestBody SetupTemplateBean bean,HttpServletRequest request) {

		Users user = authenticationService.getUser();

		boolean status = setupService.addemailData(bean, user);
		if(status) {
		activityLogService.createActivity("MAIL SETUP CREATED",
				"MAIL SETUP(" + bean.getTemplateName() + ") CREATED BY " + user.getUsername(), user, request);
		}
		return ResponseEntity.ok(status ? "Added Successfully" : "Failed!");
	}

	@PutMapping(value = "/updateEMAILData", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updateEMAILData(@RequestBody SetupTemplateBean bean,HttpServletRequest request) {

		Users user = authenticationService.getUser();
		boolean status = setupService.updateEmailData(bean, user);
		if(status) {
		activityLogService.createActivity("MAIL SETUP UPDATED",
				"MAIL SETUP(" + bean.getTemplateName() + ") UPDATED BY " + user.getUsername(), user, request);
		}

		return ResponseEntity.ok(status ? "EMAIL Updated Successfully" : "Failed!");
	}

	@GetMapping("/getGPRSdata")
	public ResponseEntity<Map<String, Object>> getGPRSData(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String search) {
		Users user = authenticationService.getUser();
		Map<String, Object> response = setupService.getGprsData(user, page, size, search);
		return ResponseEntity.ok(response);
	}

	// CREATE
	@PostMapping(value = "/addGPRSData", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> addGPRSData(@RequestBody SetupTemplateBean bean,HttpServletRequest request) {

		Users user = authenticationService.getUser();

		boolean status = setupService.addgprsData(bean, user);
		if(status) {
		activityLogService.createActivity("GPS SETUP CREATED",
				"GPS SETUP(" + bean.getTemplateName() + ") CREATED BY " + user.getUsername(), user, request);
		}
		return ResponseEntity.ok(status ? "Added Successfully" : "Failed!");
		
	}

	@PutMapping(value = "/updateGPRSData", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updateGPRSData(@RequestBody SetupTemplateBean bean,HttpServletRequest request) {

		Users user = authenticationService.getUser();
		boolean status = setupService.updategprsData(bean, user);
		if(status) {
		activityLogService.createActivity("GPS SETUP UPDATED",
				"GPS SETUP(" + bean.getTemplateName() + ") UPDATED BY " + user.getUsername(), user, request);
		}
		return ResponseEntity.ok(status ? "GPRS Updated Successfully" : "Failed!");
	}

	@DeleteMapping("/deletetemplate")
	public ResponseEntity<String> deleteTemplate(@RequestParam("id") long id,HttpServletRequest request) {

		Users user = authenticationService.getUser();

		boolean status = setupService.deleteTemplate(id, user);
		if(status) {
		activityLogService.createActivity("TEMPLATE DELETED",
				"TEMPLATE DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(), user, request);
		}

		return ResponseEntity.ok(status ? "Template deleted successfully" : "Failed!");
	}

	@PutMapping(value = "/updateuserdatasetup", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updateuserdatasetup(@RequestBody CustomUserDTO bean,HttpServletRequest request) {

		Users user = authenticationService.getUser();

		if (user == null) {
			return ResponseEntity.status(401).body("Unauthorized");
		}

		boolean status = setupService.updateuserdatasetup(bean, user);

		if (status) {
			user.setAvailableWidgets(bean.getAvailableWidgets());
			user.setDashboardMenu(bean.getDashboardMenu());
			activityLogService.createActivity("USER SETUP UPDATED",
						"USER SETUP(" + bean.getUsername() + ") UPDATED BY " + user.getUsername(), user, request);

			return ResponseEntity.ok("Updated Successfully");
		}

		return ResponseEntity.badRequest().body("Failed!");
	}

	@GetMapping("/getObjectdata")
	public ResponseEntity<Map<String, Object>> getObjectData(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String search) {
		Users user = authenticationService.getUser();
		Map<String, Object> response = setupService.getObjectData(user, page, size, search);
		return ResponseEntity.ok(response);
	}
    @GetMapping("/allDriver")
    public Map<Long, String> getAllDriver() {
        return setupService.getAllDriver();
    }
}
