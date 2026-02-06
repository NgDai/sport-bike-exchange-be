package com.bicycle.marketplace.Repository;

import com.bicycle.marketplace.entities.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICheckInRepository extends JpaRepository<CheckIn, Integer> {
}
