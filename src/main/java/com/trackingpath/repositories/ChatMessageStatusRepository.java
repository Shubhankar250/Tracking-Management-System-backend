package com.trackingpath.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackingpath.entities.ChatMessageStatus;

public interface ChatMessageStatusRepository extends JpaRepository<ChatMessageStatus, Long> {
	@Query("""
			SELECT COUNT(s) 
			FROM ChatMessageStatus s 
			WHERE s.username = :username 
			AND s.readAt IS NULL
			""")
			long countUnread(@Param("username") String username);
	
	@Modifying
	@Query("""
	UPDATE ChatMessageStatus s 
	SET s.readAt = CURRENT_TIMESTAMP 
	WHERE s.username = :username 
	AND s.messageId IN (
	    SELECT m.id FROM ChatMessage m 
	    WHERE m.conversationId = :conversationId
	)
	AND s.readAt IS NULL
	""")
	void markAsRead(
	    @Param("username") String username,
	    @Param("conversationId") Long conversationId
	);
	
	@Query("""
			SELECT m.senderUsername, COUNT(m.id)
			FROM ChatMessage m
			WHERE m.receiverUsername = :username
			AND NOT EXISTS (
			    SELECT s FROM ChatMessageStatus s
			    WHERE s.messageId = m.id
			    AND s.username = :username
			    AND s.readAt IS NOT NULL
			)
			GROUP BY m.senderUsername
			""")
			List<Object[]> getUnreadByUser(@Param("username") String username);
}

