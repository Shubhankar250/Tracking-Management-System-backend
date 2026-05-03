package com.trackingpath.controllers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.dtos.PositionUpdate;

@RestController
public class PositionSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public PositionSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/api/push-position")
    public void push(@RequestBody PositionUpdate update) {
        messagingTemplate.convertAndSend("/topic/positions", update);
    }
}
