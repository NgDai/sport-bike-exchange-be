package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICheckInRepository extends JpaRepository<CheckIn, Integer> {
//    List<CheckIn> findByStatus(String status);

    Optional<CheckIn> findByToken(String token);
}
