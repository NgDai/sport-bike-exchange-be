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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        var existingWishlist = wishlistRepository.findByUser_UserIdAndListing_ListingId(userId, listingId);
        if (existingWishlist.isEmpty()){
            return new WishlistToggleResponse(false, "Not in wishlist");
        }
        wishlistRepository.delete(existingWishlist.get());
        return new WishlistToggleResponse(true, "Removed from wishlist");
    }

    // Toggle: thêm nếu chưa có, xóa nếu đã có
    public WishlistToggleResponse toggleWishlist(int userId, int listingId) {
        var existing = wishlistRepository.findByUser_UserIdAndListing_ListingId(userId, listingId);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return new WishlistToggleResponse(false, "Removed from wishlist");
        } else {
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            BikeListing listing = bikeListingRepository.findById(listingId)
                    .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setListing(listing);
            wishlistRepository.save(wishlist);
            return new WishlistToggleResponse(true, "Added to wishlist");
        }
    }

    public boolean isInWishlist(int userId, int listingId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return wishlistRepository.findByUser_UserIdAndListing_ListingId(userId, listingId).isPresent();
    }

    // Lấy wishlist theo userId (trả DTO, không trả Entity)
    public List<WishlistResponse> getWishlistByUserId(int userId) {
        return wishlistRepository.findAllByUser_UserId(userId)
                .stream()
                .map(wishlistMapper::toWishlistResponse)
                .collect(Collectors.toList());
    }
}
