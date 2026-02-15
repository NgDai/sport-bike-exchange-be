package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<Users, Integer> {
    boolean existsByUsername(String username);

    Optional<Users> findByUsername(String username);
}
