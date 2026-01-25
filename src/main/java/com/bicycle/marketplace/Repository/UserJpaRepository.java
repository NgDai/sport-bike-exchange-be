package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for User entity.
 * Provides database operations for user management.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username.
     *
     * @param username the username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email.
     *
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given username exists.
     *
     * @param username the username
     * @return true if user exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if a user with the given email exists.
     *
     * @param email the email address
     * @return true if user exists
     */
    boolean existsByEmail(String email);
}
