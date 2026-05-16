package com.yunus.message;

import com.yunus.enums.MessageDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findAllByConversationId(UUID conversationId, Pageable pageable);

    Page<Message> findAllByConversationIdAndContentContainingIgnoreCase(UUID conversationId, String content, Pageable pageable);

    long countByDirectionAndSentAtBetween(MessageDirection direction, LocalDateTime start, LocalDateTime end);
}
