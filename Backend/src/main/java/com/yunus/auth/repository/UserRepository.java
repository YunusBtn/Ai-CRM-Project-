package com.yunus.auth.repository;

import com.yunus.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndIsDeletedFalse(UUID userId);

    boolean existsByEmail(String email);

    Optional<User> findByIdAndIsDeletedFalseAndIsActiveTrue(UUID id);
}
