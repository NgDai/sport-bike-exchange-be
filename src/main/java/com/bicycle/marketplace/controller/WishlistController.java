package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.WishlistResponse;
import com.bicycle.marketplace.dto.response.WishlistToggleResponse;
import com.bicycle.marketplace.services.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlists")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class WishlistController {

    WishlistService wishlistService;

    // Add a listing to wishlist
    @PostMapping("/add/{listingId}")
    ApiResponse<WishlistToggleResponse> addToWishlist(@PathVariable int listingId) {
        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.addToWishlist(listingId));
        return apiResponse;
    }

    // Remove a listing from wishlist
    @DeleteMapping("/remove/{listingId}")
    ApiResponse<WishlistToggleResponse> removeFromWishlist(@PathVariable int listingId) {
        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.removeFromWishlist(listingId));
        return apiResponse;
    }

    // Toggle wishlist (add if not exists, remove if exists) - convenient for frontend
    @PostMapping("/toggle/{listingId}")
    ApiResponse<WishlistToggleResponse> toggleWishlist(@PathVariable int listingId) {
        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.toggleWishlist(listingId));
        return apiResponse;
    }

    // Check if a listing is in current user's wishlist
    @GetMapping("/check/{listingId}")
    ApiResponse<Boolean> isInWishlist(@PathVariable int listingId) {
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.isInWishlist(listingId));
        return apiResponse;
    }

    // Get current user's wishlist
    @GetMapping("/my-wishlist")
    ApiResponse<List<WishlistResponse>> getMyWishlist() {
        ApiResponse<List<WishlistResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.getMyWishlist());
        return apiResponse;
    }

    // Get wishlist item by ID
    @GetMapping("/{wishlistId}")
    ApiResponse<WishlistResponse> getWishlistById(@PathVariable int wishlistId) {
        ApiResponse<WishlistResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.getWishlistById(wishlistId));
        return apiResponse;
    }

    // Get wishlist count for a listing
    @GetMapping("/count/{listingId}")
    ApiResponse<Integer> getWishlistCount(@PathVariable int listingId) {
        ApiResponse<Integer> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.countWishlistByListing(listingId));
        return apiResponse;
    }
}
