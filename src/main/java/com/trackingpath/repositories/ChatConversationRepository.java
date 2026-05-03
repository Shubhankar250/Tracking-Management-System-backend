package com.trackingpath.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.ChatConversation;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    Optional<ChatConversation> findByConversationKey(String key);
}
