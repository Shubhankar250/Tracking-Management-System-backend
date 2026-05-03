package com.trackingpath.controllers;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.trackingpath.services.StaffService;


import com.trackingpath.dtos.StaffDto;

@RestController
@RequestMapping("/staff")
public class StaffController {
    @Autowired
    StaffService service;

    @PostMapping
    public StaffDto save(@RequestBody StaffDto dto) {
        return service.save(dto); // save + update
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {

        Map<String, Object> response = service.getStaff(page, size, search);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
    @GetMapping("/{id}")
    public StaffDto getById(@PathVariable Long id) {
        return service.getById(id);
    }
}