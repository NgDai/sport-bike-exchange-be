package com.bicycle.marketplace.Repository;

import com.bicycle.marketplace.entities.EventBicycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEventBicycleRepository extends JpaRepository<EventBicycle, Integer> {
}
