package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByNameIgnoreCase(String name);

    @Query("SELECT c.bicycleType FROM Category c")
    List<String> findAllBicycleTypes();
}
