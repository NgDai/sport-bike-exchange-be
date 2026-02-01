package com.bicycle.marketplace.Repository;

import com.bicycle.marketplace.entity.BikeListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBikeListingRepository extends JpaRepository<BikeListing, Integer> {
}
