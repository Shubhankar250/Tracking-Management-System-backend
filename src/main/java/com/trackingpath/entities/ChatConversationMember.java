package com.trackingpath.entities;

import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "chat_conversation_member")
@Data
public class ChatConversationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long conversationId;

    private String username;

    private String roleCode;

    private Boolean isMuted = false;
    private Boolean isArchived = false;

    private Instant joinedAt = Instant.now();

    // getters/setters
}