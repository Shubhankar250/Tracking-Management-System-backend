package com.trackingpath.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_conv_sentat", columnList = "conversationId,sentAt"),
        @Index(name = "idx_chat_sender", columnList = "senderUsername"),
        @Index(name = "idx_chat_receiver", columnList = "receiverUsername")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long conversationId;

    @Column(nullable = false, length = 100)
    private String senderUsername;

    @Column(nullable = false, length = 50)
    private String senderRole;

    @Column(nullable = false, length = 100)
    private String receiverUsername;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(nullable = false, length = 20)
    private String messageType;

    @Column(nullable = false)
    private Instant sentAt;

    @PrePersist
    public void prePersist() {
        if (sentAt == null) {
            sentAt = Instant.now();
        }
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
