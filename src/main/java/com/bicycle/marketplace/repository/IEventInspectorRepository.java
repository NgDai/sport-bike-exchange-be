package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.EventInspector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEventInspectorRepository extends JpaRepository<EventInspector, Integer> {

}
