package com.yunus.conversation;

import com.yunus.dashboard.dto.ConversationStatusCountResponse;
import com.yunus.enums.ConversationStatus;
import com.yunus.enums.MessageDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID>, JpaSpecificationExecutor<Conversation> {


    Optional<Conversation> findByIdAndIsDeletedFalse(UUID id);

    Page<Conversation> findAllByCustomerIdAndIsDeletedFalse(UUID customerId, Pageable pageable);

    Page<Conversation> findAllByAssignedToIdAndIsDeletedFalse(UUID assignedToId, Pageable pageable);

    Page<Conversation> findAllByIsDeletedFalse(Pageable pageable);

    Page<Conversation> findAllByAssignedToIsNullAndStatusInAndIsDeletedFalse(List<ConversationStatus> status, Pageable pageable);

    @Query(
            value = """
                SELECT DISTINCT m.conversation
                FROM Message m
                WHERE m.conversation.isDeleted = false
                  AND m.conversation.status IN :statuses
                  AND m.direction = :direction
                  AND m.sentAt = (
                      SELECT MAX(m2.sentAt)
                      FROM Message m2
                      WHERE m2.conversation = m.conversation
                  )
                  """,
            countQuery = """
      SELECT COUNT(DISTINCT m.conversation)
                    FROM Message m
                    WHERE m.conversation.isDeleted = false
                      AND m.conversation.status IN :statuses
                      AND m.direction = :direction
                      AND m.sentAt = (
                          SELECT MAX(m2.sentAt)
                          FROM Message m2
                          WHERE m2.conversation = m.conversation
                      )""")
    Page<Conversation> findWaitingConversations(
            @Param("statuses") List<ConversationStatus> statuses,
            @Param("direction") MessageDirection direction,
            Pageable pageable);


    long countByStatusAndIsDeletedFalse(ConversationStatus status);

    long countByAssignedToIsNullAndStatusInAndIsDeletedFalse(List<ConversationStatus> status);

    long countByAssignedToIdAndStatusAndIsDeletedFalse(UUID assignedToId,ConversationStatus status);

    long countByStatusAndUpdatedAtBetweenAndIsDeletedFalse(ConversationStatus status, LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT new com.yunus.dashboard.dto.ConversationStatusCountResponse(
                c.status,
                COUNT(c.id)
            )
            FROM Conversation c
            WHERE c.isDeleted = false
            GROUP BY c.status
            """)
    List<ConversationStatusCountResponse> countConversationsByStatus();

    @Query("""
            SELECT COUNT(DISTINCT m.conversation)
            FROM Message m
            WHERE m.conversation.isDeleted = false
              AND m.conversation.status IN :statuses
              AND m.direction = :direction
              AND m.sentAt = (
                  SELECT MAX(m2.sentAt)
                  FROM Message m2
                  WHERE m2.conversation = m.conversation
              )
            """)
    long countWaitingConversations(
            @Param("statuses") List<ConversationStatus> statuses,
            @Param("direction") MessageDirection direction
    );

}










