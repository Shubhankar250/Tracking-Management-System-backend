package com.trackingpath.repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderBySentAtAsc(Long conversationId);

    List<ChatMessage> findTop20BySenderUsernameOrReceiverUsernameOrderBySentAtDesc(String sender, String receiver);
}
