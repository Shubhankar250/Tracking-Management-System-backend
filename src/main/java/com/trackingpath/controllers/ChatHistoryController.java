package com.trackingpath.controllers;

import com.trackingpath.dtos.ChatMessageResponse;
import com.trackingpath.dtos.UserWithDeviceDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.UserRepository;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.ChatService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {
	@Autowired
	UserRepository userRepository;
    private final ChatService chatService;

    public ChatHistoryController(ChatService chatService) {
        this.chatService = chatService;
    }
    @Autowired
   	AuthenticationService authenticationService;
    @GetMapping("/history/{conversationId}")
    public List<ChatMessageResponse> history(@PathVariable Long conversationId) {

        return chatService.getConversationHistory(conversationId);
    }
    @GetMapping("/conversation/{userId}")
    public Long getConversation(@PathVariable Long userId) {
    	 Users user = authenticationService.getCurrentUser();
        return chatService.getOrCreateConversationForDevice(userId,user);
    }
    @PostMapping("/upload")
    public ChatMessageResponse uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long conversationId,
            @RequestParam String receiverUsername        
    ) {
    	 Users user = authenticationService.getCurrentUser();
        return chatService.handleFileUpload(file, conversationId, receiverUsername, user);
    }
    @GetMapping("/userslist")
    public List<UserWithDeviceDTO> getCombinedList() {
        Users user = authenticationService.getCurrentUser();
        return chatService.getCombinedList(user);
    }
    @GetMapping("/unread-count")
    public long getUnread() {
    	 Users user = authenticationService.getCurrentUser();
        return chatService.getUnreadCount(user.getUsername());
    }
    
    @PostMapping("/mark-read/{conversationId}")
    public void markRead(
            @PathVariable Long conversationId) {
    	Users user = authenticationService.getCurrentUser();
        chatService.markConversationAsRead(user.getUsername(), conversationId);
    }
    @GetMapping("/unread-by-user")
    public Map<String, Long> getUnreadByUser() {
        Users user = authenticationService.getCurrentUser();
        return chatService.getUnreadCountByUser(user.getUsername());
    }
    @PostMapping("/heartbeat")
	public ResponseEntity<?> heartbeat() {

	    Users user = authenticationService.getCurrentUser();

	    userRepository.updateLoggedInByUsername(user.getUsername());

	    return ResponseEntity.ok().build();
	}
    @PostMapping("/logout")
	public ResponseEntity<?> logout() {
    	  Users user = authenticationService.getCurrentUser();

  	    userRepository.updateLoggedOutByUsername(user.getUsername());

  	    return ResponseEntity.ok().build();
}   
    @GetMapping("/status-by-username")
    public ResponseEntity<Map<String, String>> getStatusByUsername() {

        List<Users> users = userRepository.findAll();

        Map<String, String> statusMap = new HashMap<>();

        for (Users user : users) {
            String status = chatService.getUserStatus(user);
            statusMap.put(user.getUsername(), status);
        }

        return ResponseEntity.ok(statusMap);
    }
}