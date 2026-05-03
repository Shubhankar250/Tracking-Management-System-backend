package com.trackingpath.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.trackingpath.dtos.ExternalAccessTokenDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.responses.ExternalTokenResponse;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.ZlmService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/zlm")
public class ZlmController {
	  @Autowired
	    private ActivityLogService activityLogService;
	    @Autowired
	    AuthenticationService authenticationService;
    private final ZlmService service;

    public ZlmController(ZlmService service) {
        this.service = service;
    }

   
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ExternalAccessTokenDTO saveOrUpdate(
            @RequestBody ExternalAccessTokenDTO dto) {
        return service.save(dto);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTokens(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {

        Map<String, Object> response = service.getAllTokens(page, size, search);
        return ResponseEntity.ok(response);
    }

    
    // DELETE
    @DeleteMapping("/{id}")
    public String deleteTokendata(@PathVariable long id, HttpServletRequest request) {

        Users user = authenticationService.getCurrentUser();

        boolean status = service.deleteById(id);

        if (status) {
            activityLogService.createActivity(
                    "Tokendata DELETED",
                    "Tokendata DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(),
                    user,
                    request
            );
            return "Deleted Successfully";
        } else {
            return "Token not found!";
        }
    }

    
    @PostMapping("/fetch-token")
    public ResponseEntity<ExternalTokenResponse> fetchToken(
                    @RequestParam String projectName) {

            return ResponseEntity.ok(
                    service.fetchAndSaveToken(projectName)
            );
    }
    
    
    // GET TOKEN
    @GetMapping("/token")
    public ExternalAccessTokenDTO getToken() {
        return service.getTokenByProject();
    }
}
