package com.bicycle.marketplace.Repository;


import com.bicycle.marketplace.entities.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IEventRepository extends JpaRepository<Events, Integer> {
    Optional<Events> findById(int eventId);
}
