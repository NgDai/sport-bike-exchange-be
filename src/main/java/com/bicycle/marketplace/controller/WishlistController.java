package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.WishlistResponse;
import com.bicycle.marketplace.dto.response.WishlistToggleResponse;
import com.bicycle.marketplace.entities.Wishlist;
import com.bicycle.marketplace.services.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlists")
public class WishlistController {
    @Autowired
    private WishlistService wishlistService;

    @PostMapping("/add")
    ApiResponse<WishlistToggleResponse> addToWishlist(@RequestParam int userId, @RequestParam int listingId) {
        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.addToWishlist(userId, listingId));
        return apiResponse;
    }

    @DeleteMapping("/remove/{wishlistId}")
    ApiResponse<WishlistToggleResponse> removeFromWishlist(int userId, int listingId) {
        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.removeFromWishlist(userId, listingId));
        return apiResponse;
    }

    @GetMapping("/check/{userId}/{listingId}")
    ApiResponse<Boolean> isInWishlist(int userId, int listingId) {
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.isInWishlist(userId, listingId));
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<Wishlist>> getAllWishlist() {
        ApiResponse<List<Wishlist>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.getAllWishlist());
        return apiResponse;
    }

    @GetMapping("/{wishlistId}")
    ApiResponse<WishlistResponse> getWishlistById(@PathVariable int wishlistId) {
        ApiResponse<WishlistResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.getWishlistById(wishlistId));
        return apiResponse;
    }
}
