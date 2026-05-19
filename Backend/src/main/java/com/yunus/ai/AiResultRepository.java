package com.yunus.ai;

import com.yunus.enums.AiResultStatus;
import com.yunus.enums.AiResultType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiResultRepository extends JpaRepository<AiResult, UUID> {

    Page<AiResult> findAllByConversationIdOrderByCreatedAtDesc(
            UUID conversationId,
            Pageable pageable
    );

    Optional<AiResult> findTopByConversationIdAndTypeAndStatusOrderByCreatedAtDesc(
            UUID conversationId,
            AiResultType type,
            AiResultStatus status
    );
}
