package com.trackingpath.controllers;

import com.trackingpath.dtos.AiAgentRequest;
import com.trackingpath.dtos.AiAgentResponse;
import com.trackingpath.services.AiAgentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/ai-agent")
public class AiAgentController {

    private final AiAgentService aiAgentService;

    public AiAgentController(AiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    @PostMapping(
            value = {"/ask", "/chat"},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AiAgentResponse> ask(@Valid @RequestBody AiAgentRequest request) {
        try {
            AiAgentResponse response = aiAgentService.ask(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            AiAgentResponse response = new AiAgentResponse(
                    ex.getMessage(),
                    "error",
                    Instant.now()
            );
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }
}
