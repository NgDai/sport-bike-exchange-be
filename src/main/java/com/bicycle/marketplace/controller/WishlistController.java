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

    // ✅ Lấy wishlist của user đang đăng nhập
    @GetMapping("/my")
    ApiResponse<List<WishlistResponse>> getMyWishlist(Authentication auth) {
        int userId = getUserIdFromToken(auth);
        ApiResponse<List<WishlistResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.getWishlistByUserId(userId));
        return apiResponse;
    }

    // ✅ Thêm vào wishlist — userId lấy từ token
    @PostMapping("/add/{listingId}")
    ApiResponse<WishlistToggleResponse> addToWishlist(
            @PathVariable int listingId, Authentication auth) {
        int userId = getUserIdFromToken(auth);
        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.addToWishlist(userId, listingId));
        return apiResponse;
    }

    // ✅ Xóa khỏi wishlist — userId lấy từ token
    @DeleteMapping("/remove/{listingId}")
    ApiResponse<WishlistToggleResponse> removeFromWishlist(
            @PathVariable int listingId, Authentication auth) {
        int userId = getUserIdFromToken(auth);
        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.removeFromWishlist(userId, listingId));
        return apiResponse;
    }

    // ✅ Toggle thêm/xóa — endpoint quan trọng nhất cho FE
    @PostMapping("/toggle/{listingId}")
    ApiResponse<WishlistToggleResponse> toggleWishlist(
            @PathVariable int listingId, Authentication auth) {
        int userId = getUserIdFromToken(auth);
        ApiResponse<WishlistToggleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.toggleWishlist(userId, listingId));
        return apiResponse;
    }

    // ✅ Kiểm tra — có @PathVariable đúng
    @GetMapping("/check/{listingId}")
    ApiResponse<Boolean> isInWishlist(
            @PathVariable int listingId, Authentication auth) {
        int userId = getUserIdFromToken(auth);
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        apiResponse.setResult(wishlistService.isInWishlist(userId, listingId));
        return apiResponse;
    }

    // Helper: lấy userId từ JWT token
    private int getUserIdFromToken(Authentication auth) {
        // Tùy cách bạn encode token, ví dụ:
        // return Integer.parseInt(auth.getName());
        // hoặc cast sang UserDetails custom
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
