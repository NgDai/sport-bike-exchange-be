package com.bicycle.marketplace.Repository;


import com.bicycle.marketplace.entity.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEventRepository extends JpaRepository<Events, Integer> {
}
