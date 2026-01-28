package com.bicycle.marketplace.Repository;

import com.bicycle.marketplace.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<Users, Integer> {
    boolean existsByUsername (String username);
    Optional<Users> findByUsername(String username);
}
