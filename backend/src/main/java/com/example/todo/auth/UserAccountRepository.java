package com.example.todo.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    boolean existsByEmail(String email);
    Optional<UserAccount> findByEmail(String email);
}
