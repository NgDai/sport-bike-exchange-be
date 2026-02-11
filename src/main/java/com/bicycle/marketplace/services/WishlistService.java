package com.bicycle.marketplace.services;

import com.bicycle.marketplace.Repository.IBikeListingRepository;
import com.bicycle.marketplace.Repository.IUserRepository;
import com.bicycle.marketplace.Repository.IWishlistRepository;
import com.bicycle.marketplace.dto.response.WishlistResponse;
import com.bicycle.marketplace.dto.response.WishlistToggleResponse;
import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.entities.Wishlist;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.WishlistMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {
    @Autowired
    private IWishlistRepository wishlistRepository;
    @Autowired
    private WishlistMapper wishlistMapper;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IBikeListingRepository bikeListingRepository;

    public WishlistToggleResponse addToWishlist(int userId, int listingId){
        var existingWishlist = wishlistRepository.findByUser_UserIdAndListing_ListingId(userId, listingId);
        if (existingWishlist.isPresent()){
            return new WishlistToggleResponse(false, "Already in wishlist");
        }

        Users user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        BikeListing listing = bikeListingRepository.findById(listingId).orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setListing(listing);
        wishlistRepository.save(wishlist);
        return new WishlistToggleResponse(true, "Added to wishlist");
    }

    public WishlistToggleResponse removeFromWishlist(int userId, int listingId){
        var existingWishlist = wishlistRepository.findByUser_UserIdAndListing_ListingId(userId, listingId);
        if (existingWishlist.isEmpty()){
            return new WishlistToggleResponse(false, "Not in wishlist");
        }
        wishlistRepository.delete(existingWishlist.get());
        return new WishlistToggleResponse(true, "Removed from wishlist");
    }

    public boolean isInWishlist(int userId, int listingId){
        return wishlistRepository.findByUser_UserIdAndListing_ListingId(userId, listingId).isPresent();
    }

    public List<Wishlist> getAllWishlist() {
        return wishlistRepository.findAll();
    }

    public WishlistResponse getWishlistById(int wishlistId) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new AppException(ErrorCode.WISHLIST_NOT_FOUND));
        return wishlistMapper.toWishlistResponse(wishlist);
    }
}
