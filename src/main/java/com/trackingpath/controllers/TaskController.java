package com.trackingpath.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.DeliveryDto;
import com.trackingpath.dtos.DeviceModalDTO;
import com.trackingpath.dtos.PickupDTO;
import com.trackingpath.dtos.TaskDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.exceptions.FileSizeExceededException;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.TaskService;

import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ActivityLogService activityLogService;
    // ================= TASK APIs =================
	@Value("${IMAGE_MAX_SIZE}")
	private long IMAGE_MAX_SIZE;
	@Value("${FILE_UPLOAD_PATH}")
	private String FILE_UPLOAD_PATH;
    // CREATE TASK
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createTask(@RequestBody TaskDTO bean,HttpServletRequest request) {
        Users user = authenticationService.getUser();
        boolean status = taskService.addTaskData(bean, user);
        activityLogService.createActivity("NEW TASK CREATED",
				"TASK(" + bean.getName() + ") CREATED BY " + user.getUsername(), user, request);

        return status
                ? ResponseEntity.ok("New Task added successfully!")
                : ResponseEntity.badRequest().body("Failed to add task");
    }

    // UPDATE TASK
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateTask(@RequestBody TaskDTO bean,HttpServletRequest request) {
    	 Users user = authenticationService.getUser();
        boolean status = taskService.updateTaskData(bean);
        activityLogService.createActivity("TASK UPDATED",
				"TASK(" + bean.getName() + ") UPDATED BY " + user.getUsername(), user, request);

        return status
                ? ResponseEntity.ok("Task updated successfully!")
                : ResponseEntity.badRequest().body("Failed to update task");
    }

    // DELETE TASK
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable long id,HttpServletRequest request) {
        Users user = authenticationService.getUser();
        boolean status = taskService.deleteTaskService(id, user);
        activityLogService.createActivity("TASK DELETED",
				"TASK DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(), user, request);

        return status
                ? ResponseEntity.ok("Task deleted successfully!")
                : ResponseEntity.badRequest().body("Failed to delete task");
    }

    // GET TASK LIST
 // GET TASK LIST - Now supports "Show All" when dates are not provided
    @GetMapping
    public ResponseEntity<Page<TaskDTO>> getTasks(
            @RequestParam(required = false) String start_time,     // ← Made optional
            @RequestParam(required = false) String end_time,       // ← Made optional
            @RequestParam(defaultValue = "0") long deviceId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "25") int size,
            @RequestParam(required = false, defaultValue = "") String search) {

        Users user = authenticationService.getUser();
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
            taskService.getTaskData(user, start_time, end_time, deviceId, search, pageable)
        );
    }


    // GET TASK BY ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable long id) {
        return ResponseEntity.ok(taskService.getTaskUpdate(id));
    }
    
 // UPDATE PICKUP DATA
    @PutMapping(value = "/pickup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updatePickupdata(
            @RequestPart("data") String data,
            @RequestPart(value = "pickup_image", required = false) MultipartFile pickup_image,
            HttpServletRequest request) {

        try {
            // JSON → DTO
            ObjectMapper mapper = new ObjectMapper();
            PickupDTO bean = mapper.readValue(data, PickupDTO.class);

            // handle image
            if (pickup_image != null && !pickup_image.isEmpty()) {
                bean.setPickupImage(saveFile(pickup_image, IMAGE_MAX_SIZE, "pickup_image"));
            }

            boolean status = taskService.updatePickupdata(bean);

            return status
                    ? ResponseEntity.ok("Pickup data updated successfully!")
                    : ResponseEntity.badRequest().body("Failed to update pickup data");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    // UPDATE DELIVERED DATA
    @PutMapping(value = "/delivered", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updatedelivereddata(
            @RequestPart("data") String data,
            @RequestPart(value = "deliveryimage", required = false) MultipartFile deliveryimage,
            HttpServletRequest request) {

        try {
            // Convert JSON → DTO
            ObjectMapper mapper = new ObjectMapper();
            DeliveryDto bean = mapper.readValue(data, DeliveryDto.class);

            // Handle file upload
            if (deliveryimage != null && !deliveryimage.isEmpty()) {
                bean.setDeliveryImage(saveFile(deliveryimage, IMAGE_MAX_SIZE, "deliveryimage"));
            }

            // Call service
            boolean status = taskService.updatedelivereddata(bean);

            return status
                    ? ResponseEntity.ok("Delivery data updated successfully!")
                    : ResponseEntity.badRequest().body("Failed to update delivery data");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
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
}
