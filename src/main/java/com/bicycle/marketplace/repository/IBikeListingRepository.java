package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBikeListingRepository extends JpaRepository<BikeListing, Integer> {
    List<BikeListing> findBySeller(Users seller);

    List<BikeListing> findBySellerAndStatus(Users seller, String status);

    long countBySeller(Users seller);
}
