package com.trackingpath.controllers;



import com.trackingpath.dtos.ChatInboundMessage;
import com.trackingpath.entities.Users;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.ChatService;

import jakarta.validation.Valid;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }
    @Autowired
	AuthenticationService authenticationService;

    @MessageMapping("/chat.send")
    public void send(@Valid ChatInboundMessage inbound, Principal principal) {

        System.out.println("🔥 MESSAGE FROM: " + principal.getName());

        chatService.saveAndSend(inbound, principal);
    }
}