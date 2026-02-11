package com.bicycle.marketplace.Repository;

import com.bicycle.marketplace.entities.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBrandRepository extends JpaRepository<Brand, Integer> {
}
