package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trackingpath.entities.ChatConversationMember;

public interface ChatConversationMemberRepository extends JpaRepository<ChatConversationMember, Long> {
    List<ChatConversationMember> findByConversationId(Long conversationId);
    boolean existsByConversationIdAndUsername(Long conversationId, String username);
}