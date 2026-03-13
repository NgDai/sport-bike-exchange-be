package com.bicycle.marketplace.services;

import com.bicycle.marketplace.repository.IBikeListingRepository;
import com.bicycle.marketplace.repository.IUserRepository;
import com.bicycle.marketplace.repository.IWishlistRepository;
import com.bicycle.marketplace.dto.response.WishlistResponse;
import com.bicycle.marketplace.dto.response.WishlistToggleResponse;
import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.entities.Wishlist;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.WishlistMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final IWishlistRepository wishlistRepository;
    private final WishlistMapper wishlistMapper;
    private final IUserRepository userRepository;
    private final IBikeListingRepository bikeListingRepository;

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public WishlistToggleResponse addToWishlist(int listingId) {
        Users user = getCurrentUser();
        var existingWishlist = wishlistRepository.findByUser_UserIdAndListing_ListingId(user.getUserId(), listingId);
        if (existingWishlist.isPresent()) {
            return new WishlistToggleResponse(false, "Already in wishlist");
        }

        BikeListing listing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .listing(listing)
                .build();
        wishlistRepository.save(wishlist);
        return new WishlistToggleResponse(true, "Added to wishlist");
    }

    public WishlistToggleResponse removeFromWishlist(int listingId) {
        Users user = getCurrentUser();
        var existingWishlist = wishlistRepository.findByUser_UserIdAndListing_ListingId(user.getUserId(), listingId);
        if (existingWishlist.isEmpty()) {
            return new WishlistToggleResponse(false, "Not in wishlist");
        }
        wishlistRepository.delete(existingWishlist.get());
        return new WishlistToggleResponse(true, "Removed from wishlist");
    }

    public WishlistToggleResponse toggleWishlist(int listingId) {
        Users user = getCurrentUser();
        var existingWishlist = wishlistRepository.findByUser_UserIdAndListing_ListingId(user.getUserId(), listingId);
        if (existingWishlist.isPresent()) {
            wishlistRepository.delete(existingWishlist.get());
            return new WishlistToggleResponse(false, "Removed from wishlist");
        }

        BikeListing listing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .listing(listing)
                .build();
        wishlistRepository.save(wishlist);
        return new WishlistToggleResponse(true, "Added to wishlist");
    }

    public boolean isInWishlist(int listingId) {
        Users user = getCurrentUser();
        return wishlistRepository.findByUser_UserIdAndListing_ListingId(user.getUserId(), listingId).isPresent();
    }

    public List<WishlistResponse> getMyWishlist() {
        Users user = getCurrentUser();
        List<Wishlist> wishlists = wishlistRepository.findAllByUser_UserId(user.getUserId());
        return wishlists.stream()
                .map(wishlistMapper::toWishlistResponse)
                .collect(Collectors.toList());
    }

    public WishlistResponse getWishlistById(int wishlistId) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new AppException(ErrorCode.WISHLIST_NOT_FOUND));
        return wishlistMapper.toWishlistResponse(wishlist);
    }

    public int countWishlistByListing(int listingId) {
        return wishlistRepository.countByListing_ListingId(listingId);
    }
}
