package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBikeListingRepository extends JpaRepository<BikeListing, Integer> {
    List<BikeListing> findBySeller(Users seller);

    @Query("""
    SELECT b FROM BikeListing b
    WHERE b.seller = :seller
    AND b.status = 'Available'
    AND NOT EXISTS (
        SELECT 1 FROM EventBicycle e WHERE e.listing = b
    )
""")
    List<BikeListing> findBySellerWithEvent(@Param("seller") Users seller);

    long countBySeller(Users seller);
}
