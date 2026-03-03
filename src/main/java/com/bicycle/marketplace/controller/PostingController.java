package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.CreatePostingRequest;
import com.bicycle.marketplace.dto.request.UpdatePostingRequest;
import com.bicycle.marketplace.dto.request.UpdatePostingStatusRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.PostingResponse;
import com.bicycle.marketplace.entities.BikeListing;
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

    @PostMapping("/create")
    ApiResponse<BikeListing> createPosting(@RequestBody CreatePostingRequest request) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.createPosting(request));
        return apiResponse;
    }

    @PutMapping("/update/{listingId}")
    ApiResponse<BikeListing> updatePosting(@RequestBody UpdatePostingRequest request, @PathVariable int listingId) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
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

    @DeleteMapping("/delete/{listingId}")
    ApiResponse<Void> deletePosting(@PathVariable int listingId) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        postingService.deletePosting(listingId);
        return apiResponse;
    }

    @PutMapping("/updateStatus/{listingId}")
    ApiResponse<BikeListing> updatePostingStatus(@PathVariable int listingId, @RequestBody UpdatePostingStatusRequest status) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.updatePostingStatus(listingId, status));
        return apiResponse;
    }

}
