package com.yunus.note;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    Optional<Note> findById(UUID id);
    Page<Note> findAllByCustomerId(UUID customerId, Pageable pageable);
    Page<Note> findAllByConversationId(UUID conversationId,Pageable pageable);
}
