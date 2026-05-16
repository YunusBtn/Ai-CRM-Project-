package com.yunus.tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);

    Optional<Tag> findByIdAndIsDeletedFalse(UUID id);

    Page<Tag> findAllByIsDeletedFalse(Pageable pageable);
}
