package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.EventBicycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEventBicycleRepository extends JpaRepository<EventBicycle, Integer> {
}
