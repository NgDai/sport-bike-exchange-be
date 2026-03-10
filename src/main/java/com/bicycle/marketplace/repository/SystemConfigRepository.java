package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, Integer> {
        Optional<SystemConfig> findByKey(String key);
}
