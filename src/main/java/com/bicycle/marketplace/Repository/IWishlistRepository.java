package com.bicycle.marketplace.Repository;

import com.bicycle.marketplace.entities.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IWishlistRepository extends JpaRepository<Wishlist, Integer> {
    Optional<Wishlist> findByUser_UserIdAndListing_ListingId(int userId, int listingId);
}
