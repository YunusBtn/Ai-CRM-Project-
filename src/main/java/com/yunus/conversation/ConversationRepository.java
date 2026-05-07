package com.yunus.conversation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {


    Optional<Conversation> findByIdAndIsDeletedFalse(UUID id);

    Page<Conversation> findAllByIsDeletedFalse(Pageable pageable);

    Page<Conversation> findAllByCustomerIdAndIsDeletedFalse(UUID customerId, Pageable pageable);


}
