package com.trackingpath.dtos;
import java.time.Instant;

public class ChatMessageResponse {

    private Long id;
    private Long conversationId;
    private String senderUsername;
    private String senderRole;
    private String receiverUsername;
    private String content;
    private String messageType;
    private Instant sentAt;

    public ChatMessageResponse() {
    }

    public ChatMessageResponse(Long id, Long conversationId, String senderUsername, String senderRole,
                               String receiverUsername, String content, String messageType, Instant sentAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderUsername = senderUsername;
        this.senderRole = senderRole;
        this.receiverUsername = receiverUsername;
        this.content = content;
        this.messageType = messageType;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public Long getConversationId() { return conversationId; }
    public String getSenderUsername() { return senderUsername; }
    public String getSenderRole() { return senderRole; }
    public String getReceiverUsername() { return receiverUsername; }
    public String getContent() { return content; }
    public String getMessageType() { return messageType; }
    public Instant getSentAt() { return sentAt; }

    public void setId(Long id) { this.id = id; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }
    public void setContent(String content) { this.content = content; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}
