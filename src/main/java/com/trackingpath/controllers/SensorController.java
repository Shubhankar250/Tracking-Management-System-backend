package com.trackingpath.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.dtos.DeviceSensorMappingDTO;
import com.trackingpath.dtos.SensorDTO;
import com.trackingpath.dtos.SensorTypeDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.SensorService;



@RestController
@RequestMapping("/sensors")
public class SensorController {

    @Autowired
    private SensorService sensorService;

    @Autowired
    private AuthenticationService authenticationService;

    // SENSOR TYPES
    @GetMapping("/types")
    @ResponseBody
    public List<SensorTypeDTO> getSensorType() {

        Users user = authenticationService.getUser();
        if (user != null) {
            return sensorService.getSensorType(user);
        }
        return null;
    }

    // SENSOR ATTRIBUTES BY DEVICE
    @GetMapping("/attributes")
    @ResponseBody
    public Map<String, Object> getSensorAttributes(
            @RequestParam long device_id) {

        Users user = authenticationService.getUser();
        if (user != null) {
            return sensorService.getAttributeById(device_id, user);
        }
        return null;
    }

    // CREATE SENSOR
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public String addSensor(@RequestBody SensorDTO bean) {

        Users user = authenticationService.getUser();
        if (user == null) return "Invalid User!";

        return sensorService.addSensorData(bean, user);
    }

    // READ SENSOR BY ID
    @GetMapping("/{id}")
    public DeviceSensorMappingDTO getSensor(@PathVariable long id) {

        Users user = authenticationService.getUser();
        if (user == null) return null;

        return sensorService.getSensorById(id, user);
    }

    // LIST ALL SENSORS
    @GetMapping
    public ResponseEntity<Page<DeviceSensorMappingDTO>> listSensors(
    		@RequestParam(required = false, defaultValue = "0") Long deviceId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        Users user = authenticationService.getUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(sensorService.listSensors(deviceId, user, pageable));
    }

    // UPDATE SENSOR
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public String updateSensor(@RequestBody SensorDTO bean) {

        Users user = authenticationService.getUser();
        if (user == null) return "Invalid User!";

        return sensorService.updateSensor(bean, user);
    }

    // DELETE SENSOR
    @DeleteMapping("/{id}")
    public String deleteSensor(@PathVariable Long id) {

        Users user = authenticationService.getUser();
        if (user == null) return "Invalid User!";

        return sensorService.deleteSensor(id, user);
    }
}
