package com.trackingpath.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.dtos.ApiResponseExternal;
import com.trackingpath.dtos.LiveDataResponseDTO;
import com.trackingpath.services.ApiService;

@RestController
@RequestMapping("middleMan")
public class ApiController {

    @Autowired
    private ApiService liveDataService;

    @GetMapping("/getDeviceInfo")
    public Map<String, Object> getLiveData(
            @RequestParam(required = false) String deviceId) {

        List<LiveDataResponseDTO> data = liveDataService.getUserLiveData(deviceId);

        Map<String, Object> response = new HashMap<>();
        response.put("successful", true);
        response.put("message", "");
        response.put("object", data);

        return response;
    }
}