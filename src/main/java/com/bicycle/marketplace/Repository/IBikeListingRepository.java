package com.bicycle.marketplace.Repository;

import com.bicycle.marketplace.entity.BikeListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBikeListingRepository extends JpaRepository<BikeListing, Integer> {
    List<BikeListing> findBySellerId(Integer sellerId);
    List<BikeListing> findByStatus(String status);
}
