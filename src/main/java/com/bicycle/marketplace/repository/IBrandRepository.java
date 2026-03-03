package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IBrandRepository extends JpaRepository<Brand, Integer> {
    Optional<Brand> findByNameIgnoreCase(String name);
}
