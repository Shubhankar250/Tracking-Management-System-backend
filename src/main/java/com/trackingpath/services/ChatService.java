package com.trackingpath.services;

import com.trackingpath.dtos.ChatInboundMessage;
import com.trackingpath.dtos.ChatMessageResponse;
import com.trackingpath.dtos.UserWithDeviceDTO;
import com.trackingpath.entities.ChatConversation;
import com.trackingpath.entities.ChatConversationMember;
import com.trackingpath.entities.ChatMessage;
import com.trackingpath.entities.ChatMessageStatus;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.Role;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.ChatConversationMemberRepository;
import com.trackingpath.repositories.ChatConversationRepository;
import com.trackingpath.repositories.ChatMessageRepository;
import com.trackingpath.repositories.ChatMessageStatusRepository;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.repositories.DriverRepository;
import com.trackingpath.repositories.RoleRepository;
import com.trackingpath.repositories.TaskRepository;
import com.trackingpath.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ChatMessageRepository messageRepo;
    private final ChatConversationRepository convRepo;
    private final ChatConversationMemberRepository memberRepo;
    private final ChatMessageStatusRepository statusRepo;
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    UserRepository userRepo;
    @Autowired 
    DriverRepository deriverRepo;
    
    @Autowired
    TaskRepository taskRepo;

    public ChatService(
            ChatMessageRepository messageRepo,
            ChatConversationRepository convRepo,
            ChatConversationMemberRepository memberRepo,
            ChatMessageStatusRepository statusRepo,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messageRepo = messageRepo;
        this.convRepo = convRepo;
        this.memberRepo = memberRepo;
        this.statusRepo = statusRepo;
        this.messagingTemplate = messagingTemplate;
    }

    // =====================================================
    // ✅ 1-to-1 CHAT MESSAGE
    // =====================================================
    @Transactional
    public ChatMessageResponse saveAndSend(ChatInboundMessage inbound, Principal principal) {
        // ✅ VALIDATION (user must be part of conversation)
       // if (!memberRepo.existsByConversationIdAndUsername(inbound.getConversationId(), sender)) {
           // throw new RuntimeException("User not part of this conversation");
      //  }
    	 String username = principal.getName();
    	 Authentication auth = (Authentication) principal;
         String senderRole = auth.getAuthorities().stream()
                 .findFirst()
                 .map(a -> a.getAuthority())
                 .orElse("ROLE_USER");
    	  System.out.println("username"+username+"role"+senderRole);
        // ✅ CREATE MESSAGE
        ChatMessage msg = new ChatMessage();
        msg.setConversationId(inbound.getConversationId());
        msg.setSenderUsername(username);
        msg.setReceiverUsername(inbound.getReceiverUsername());
        msg.setContent(inbound.getContent().trim());
        msg.setMessageType(inbound.getMessageType());
        msg.setSentAt(Instant.now());
        msg.setSenderRole(senderRole);

        ChatMessage saved = messageRepo.save(msg);

        // ✅ CREATE DELIVERY STATUS
        ChatMessageStatus status = new ChatMessageStatus();
        status.setMessageId(saved.getId());
        status.setUsername(inbound.getReceiverUsername());
        statusRepo.save(status);

        ChatMessageResponse response = toResponse(saved);

        // ✅ SEND TO CONVERSATION (group stream)
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + saved.getConversationId(),
                response
        );

        // ✅ SEND TO RECEIVER (personal queue)
        messagingTemplate.convertAndSendToUser(
        	    inbound.getReceiverUsername(),   // ✅ RECEIVER
        	    "/queue/inbox",
        	    response
        	);

        return response;
    }

    // =====================================================
    // ✅ BROADCAST (ADMIN → ALL USERS)
    // =====================================================
    @Transactional
    public void sendBroadcast(String content, Authentication auth) {

        String sender = auth.getName();

        // 🔒 OPTIONAL: ONLY ADMIN CAN SEND
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new RuntimeException("Only ADMIN can send broadcast messages");
        }

        // ✅ GET OR CREATE BROADCAST CONVERSATION
        ChatConversation conversation = convRepo
                .findByConversationKey("BROADCAST_GLOBAL")
                .orElseGet(this::createBroadcastConversation);

        Long convId = conversation.getId();

        // ✅ SAVE MESSAGE
        ChatMessage msg = new ChatMessage();
        msg.setConversationId(convId);
        msg.setSenderUsername(sender);
        msg.setReceiverUsername("ALL");
        msg.setContent(content.trim());
        msg.setMessageType("TEXT");
        msg.setSentAt(Instant.now());

        ChatMessage saved = messageRepo.save(msg);

        ChatMessageResponse response = toResponse(saved);

        // ✅ GET ALL MEMBERS
        List<ChatConversationMember> members = memberRepo.findByConversationId(convId);

        for (ChatConversationMember m : members) {

            if (!m.getUsername().equals(sender)) {

                // ✅ CREATE STATUS
                ChatMessageStatus status = new ChatMessageStatus();
                status.setMessageId(saved.getId());
                status.setUsername(m.getUsername());
                statusRepo.save(status);

                // ✅ PERSONAL MESSAGE
                messagingTemplate.convertAndSendToUser(
                        m.getUsername(),
                        "/queue/inbox",
                        response
                );
            }
        }

        // ✅ GLOBAL TOPIC (optional UI use)
        messagingTemplate.convertAndSend(
                "/topic/broadcast",
                response
        );
    }

    // =====================================================
    // ✅ CREATE BROADCAST CONVERSATION (AUTO)
    // =====================================================
    private ChatConversation createBroadcastConversation() {

        ChatConversation conv = new ChatConversation();
        conv.setConversationKey("BROADCAST_GLOBAL");
        conv.setConversationType("BROADCAST");
        conv.setTitle("Admin Broadcast");

        ChatConversation saved = convRepo.save(conv);

        // ⚠️ TEMP USERS (replace with DB users later)
        List<String> users = List.of("admin", "driver1", "support1");

        for (String username : users) {
            ChatConversationMember m = new ChatConversationMember();
            m.setConversationId(saved.getId());
            m.setUsername(username);
            m.setRoleCode("USER");
            memberRepo.save(m);
        }

        return saved;
    }

    // =====================================================
    // ✅ GET CHAT HISTORY
    // =====================================================
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversationHistory(Long conversationId) {
        return messageRepo.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // ✅ MAPPER
    // =====================================================
    private ChatMessageResponse toResponse(ChatMessage m) {
        return new ChatMessageResponse(
                m.getId(),
                m.getConversationId(),
                m.getSenderUsername(),
                null, // role optional (you can map if needed)
                m.getReceiverUsername(),
                m.getContent(),
                m.getMessageType(),
                m.getSentAt()
        );
    }
    @Transactional
    public Long getOrCreateConversationForDevice(Long userId, Users user) {

        Users otherUser = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ UNIQUE KEY (USER + DEVICE)
        Long min = Math.min(user.getId(), otherUser.getId());
        Long max = Math.max(user.getId(), otherUser.getId());

        String key = "USER_" + min + "_" + max;
    	  String senderRole = user.getAuthorities().stream()
                  .findFirst()
                  .map(a -> a.getAuthority())
                  .orElse("ROLE_USER");
        ChatConversation conv = convRepo.findByConversationKey(key)
                .orElseGet(() -> {
                    ChatConversation c = new ChatConversation();
                    c.setConversationKey(key);
                    c.setConversationType("PRIVATE");
                    c.setTitle("User " + userId);

                    ChatConversation saved = convRepo.save(c);

                    // ✅ ADMIN (dynamic)
                    ChatConversationMember admin = new ChatConversationMember();
                    admin.setConversationId(saved.getId());
                    admin.setUsername(user.getUsername());   // 🔥 FIXED
                    admin.setRoleCode(senderRole);
                    memberRepo.save(admin);
                                    
                    // ✅ DRIVER
                    ChatConversationMember driver = new ChatConversationMember();
                    driver.setConversationId(saved.getId());
                    driver.setUsername(otherUser.getUsername()); // mapping same
                    driver.setRoleCode("DRIVER");
                    memberRepo.save(driver);

                    return saved;
                });

        return conv.getId();
    }
    @Transactional
    public ChatMessageResponse handleFileUpload(
            MultipartFile file,
            Long conversationId,
            String receiverUsername,
            Users user
    ) {
    	String senderRole = user.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("ROLE_USER");
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            String username = user.getUsername();

            String uploadDir = "D:/chatUploads/";
           // String uploadDir = "/home/chatUploads/";
            String originalName = file.getOriginalFilename();
            if (originalName == null) originalName = "file";

            String fileName = System.currentTimeMillis() + "_" + originalName;

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            Path path = Paths.get(uploadDir + fileName);

            // ✅ SAFE COPY
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/chatUploads/" + fileName;

            ChatMessage msg = new ChatMessage();
            msg.setConversationId(conversationId);
            msg.setSenderUsername(username);
            msg.setReceiverUsername(receiverUsername);
            msg.setContent(fileUrl);
            msg.setSenderRole(senderRole); 
            msg.setMessageType("FILE");
            msg.setSentAt(Instant.now());

            ChatMessage saved = messageRepo.save(msg);

            ChatMessageResponse response = toResponse(saved);

            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + conversationId,
                    response
            );

            return response;

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 IMPORTANT (console me real error dikhega)
            throw new RuntimeException("File upload failed", e);
        }
    }
    public List<UserWithDeviceDTO> getCombinedList(Users currentUser) {

        List<UserWithDeviceDTO> result = new ArrayList<>();

        String accountname = currentUser.getAccountname();
        String username = currentUser.getUsername();

        // ================= USERS =================
        List<Object[]> users = userRepo.findUsersByAccount(accountname, username);

        users.sort(Comparator.comparing(obj -> 
                (String) obj[1], Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

        for (Object[] obj : users) {
            result.add(UserWithDeviceDTO.builder()
                    .type("USER")
                    .id((Long) obj[0])
                    .name((String) obj[1])
                    .username((String) obj[2])
                    .roleName((String) obj[4])
                    .build());
        }

        // ================= DRIVERS =================
     // ================= DRIVERS =================
        List<Object[]> drivers = deriverRepo.findDrivers();

        drivers.sort(Comparator.comparing(obj -> 
                (String) obj[1], Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

        for (Object[] obj : drivers) {
            result.add(UserWithDeviceDTO.builder()
                    .type("DRIVER")
                    .id((Long) obj[0])
                    .name((String) obj[1])
                    .username((String) obj[2])
                    .deviceName((String) obj[3])   // ✅ device name add
                    .build());
        }

        // ================= TASKS =================
        List<Object[]> tasks = taskRepo.findTasks();

        tasks.sort(Comparator.comparing(obj -> 
                (String) obj[1], Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

        for (Object[] obj : tasks) {
            result.add(UserWithDeviceDTO.builder()
                    .type("TASK")
                    .id((Long) obj[0])
                    .name((String) obj[1])
                    .username((String) obj[2])
                    .build());
        }

        return result;
    }
    
    public long getUnreadCount(String username) {
        return statusRepo.countUnread(username);
    }
    @Transactional
    public void markConversationAsRead(String username, Long conversationId) {
        statusRepo.markAsRead(username, conversationId);
    }
    public Map<String, Long> getUnreadCountByUser(String username) {

        List<Object[]> result = statusRepo.getUnreadByUser(username);

        Map<String, Long> map = new HashMap<>();

        for (Object[] row : result) {
            String senderUsername = (String) row[0];
            Long count = (Long) row[1];
            map.put(senderUsername, count);
        }

        return map;
    }
    public String getUserStatus(Users user) {

        // 🔴 OFFLINE (logout ho chuka)
        if (user.getLoggedOut() != null) {
            return "OFFLINE";
        }

        LocalDateTime lastActivity = user.getLoggedIn();

        // ❗ NULL check (IMPORTANT)
        if (lastActivity == null) {
            return "OFFLINE"; // ya "AWAY" depending on your logic
        }

        LocalDateTime now = LocalDateTime.now();

        long minutes = ChronoUnit.MINUTES.between(lastActivity, now);

        if (minutes <= 5) {
            return "ONLINE";
        }

        return "AWAY";
    }
}