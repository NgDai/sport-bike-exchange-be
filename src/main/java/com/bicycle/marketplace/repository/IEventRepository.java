package com.bicycle.marketplace.repository;


import com.bicycle.marketplace.entities.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IEventRepository extends JpaRepository<Events, Integer> {
    List<Events> findAllByStatus(String status);
}
