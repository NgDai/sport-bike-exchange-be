package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Bicycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBicycleRepository extends JpaRepository<Bicycle, Integer> {
}
