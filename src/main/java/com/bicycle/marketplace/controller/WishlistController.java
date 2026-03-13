package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.WishlistResponse;
import com.bicycle.marketplace.dto.response.WishlistToggleResponse;
import com.bicycle.marketplace.services.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlists")
public class WishlistController {
    @Autowired
    private WishlistService wishlistService;

    // Helper: lấy userId từ JWT token
    private int getUserIdFromToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return ((Number) jwt.getClaim("userId")).intValue();
    }

    // ✅ Lấy wishlist của user đang đăng nhập
    @GetMapping("/my")
    ApiResponse<List<WishlistResponse>> getMyWishlist() {
        int userId = getUserIdFromToken();
        ApiResponse<List<WishlistResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.getWishlistByUserId(userId));
        return apiResponse;
    }

    // ✅ Thêm vào wishlist — userId lấy từ token
    @PostMapping("/add/{listingId}")
    ApiResponse<WishlistToggleResponse> addToWishlist(@PathVariable int listingId) {
        int userId = getUserIdFromToken();
        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.addToWishlist(userId, listingId));
        return apiResponse;
    }

    // ✅ Xóa khỏi wishlist — userId lấy từ token
    @DeleteMapping("/remove/{listingId}")
    ApiResponse<WishlistToggleResponse> removeFromWishlist(@PathVariable int listingId) {
        int userId = getUserIdFromToken();
        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.removeFromWishlist(userId, listingId));
        return apiResponse;
    }

    // ✅ Toggle thêm/xóa — endpoint quan trọng nhất cho FE
    @PostMapping("/toggle/{listingId}")
    ApiResponse<WishlistToggleResponse> toggleWishlist(@PathVariable int listingId) {
        int userId = getUserIdFromToken();
        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.toggleWishlist(userId, listingId));
        return apiResponse;
    }

    //  Kiểm tra
    @GetMapping("/check/{listingId}")
    ApiResponse<Boolean> isInWishlist(@PathVariable int listingId) {
        int userId = getUserIdFromToken();
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.isInWishlist(userId, listingId));
        return apiResponse;
    }
}
//    @PostMapping("/add")
//    ApiResponse<WishlistToggleResponse> addToWishlist(@RequestParam int userId, @RequestParam int listingId) {
//        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(wishlistService.addToWishlist(userId, listingId));
//        return apiResponse;
//    }
//
//    @DeleteMapping("/remove/{wishlistId}")
//    ApiResponse<WishlistToggleResponse> removeFromWishlist(int userId, int listingId) {
//        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(wishlistService.removeFromWishlist(userId, listingId));
//        return apiResponse;
//    }
//
//    @GetMapping("/check/{userId}/{listingId}")
//    ApiResponse<Boolean> isInWishlist(int userId, int listingId) {
//        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(wishlistService.isInWishlist(userId, listingId));
//        return apiResponse;
//    }
//
//    @GetMapping
//    ApiResponse<List<Wishlist>> getAllWishlist() {
//        ApiResponse<List<Wishlist>> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(wishlistService.getAllWishlist());
//        return apiResponse;
//    }
//
//    @GetMapping("/{wishlistId}")
//    ApiResponse<WishlistResponse> getWishlistById(@PathVariable int wishlistId) {
//        ApiResponse<WishlistResponse> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(wishlistService.getWishlistById(wishlistId));
//        return apiResponse;
//    }
//}
