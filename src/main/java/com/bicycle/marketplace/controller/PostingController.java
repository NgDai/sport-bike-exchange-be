package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.CreatePostingRequest;
import com.bicycle.marketplace.dto.request.UpdatePostingRequest;
import com.bicycle.marketplace.dto.request.UpdatePostingStatusRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.CreatePostingResponse;
import com.bicycle.marketplace.dto.response.PostingResponse;
import com.bicycle.marketplace.services.PostingService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PostingController {

    PostingService postingService;

    @GetMapping("/calculate-fee")
    ApiResponse<Double> getListingFee(@RequestParam double price) {
        ApiResponse<Double> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.calculateListingFee(price));
        return apiResponse;
    }

    @PostMapping("/create")
    ApiResponse<CreatePostingResponse> createPosting(@RequestBody CreatePostingRequest request) {
        ApiResponse<CreatePostingResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.createPosting(request));
        return apiResponse;
    }

    // --- API MỚI: DÙNG ĐỂ THANH TOÁN LẠI NẾU BỊ HỦY GIỮA CHỪNG ---
    @PostMapping("/retry-payment/{listingId}")
    ApiResponse<CreatePostingResponse> retryPayment(@PathVariable int listingId) {
        ApiResponse<CreatePostingResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.retryPayment(listingId));
        apiResponse.setMessage("Tạo giao dịch thanh toán lại thành công");
        return apiResponse;
    }

    @PutMapping("/update/{listingId}")
    ApiResponse<PostingResponse> updatePosting(@RequestBody UpdatePostingRequest request, @PathVariable int listingId) {
        ApiResponse<PostingResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.updatePosting(request, listingId));
        return apiResponse;
    }

    @GetMapping("/{listingId}")
    ApiResponse<PostingResponse> getPostingById(@PathVariable int listingId) {
        ApiResponse<PostingResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.getPostingById(listingId));
        return apiResponse;
    }

    @GetMapping("/all")
    ApiResponse<List<PostingResponse>> getAllPostings() {
        ApiResponse<List<PostingResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.getAllPostings());
        return apiResponse;
    }

    @GetMapping("/my-posts")
    ApiResponse<List<PostingResponse>> getMyPostings() {
        ApiResponse<List<PostingResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.getMyPostings());
        return apiResponse;
    }

    @GetMapping("/events/my-posts")
    ApiResponse<List<PostingResponse>> getMyEventPostings() {
        ApiResponse<List<PostingResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.getMyPostingsForEvent());
        return apiResponse;
    }

    @DeleteMapping("/delete/{listingId}")
    ApiResponse<Void> deletePosting(@PathVariable int listingId) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        postingService.deletePosting(listingId);
        return apiResponse;
    }

    @PutMapping("/updateStatus/{listingId}")
    ApiResponse<PostingResponse> updatePostingStatus(@PathVariable int listingId, @RequestBody UpdatePostingStatusRequest status) {
        ApiResponse<PostingResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.updatePostingStatus(listingId, status));
        return apiResponse;
    }

}