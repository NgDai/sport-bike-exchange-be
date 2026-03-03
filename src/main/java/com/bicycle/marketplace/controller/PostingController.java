package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.CreatePostingRequest;
import com.bicycle.marketplace.dto.request.UpdatePostingRequest;
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

    @PutMapping("/update/{id}")
    ApiResponse<BikeListing> updatePosting(@RequestBody UpdatePostingRequest request, @PathVariable int id) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.updatePosting(request, id));
        return apiResponse;
    }

    @GetMapping("/{id}")
    ApiResponse<PostingResponse> getPostingById(@PathVariable int id) {
        ApiResponse<PostingResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.getPostingById(id));
        return apiResponse;
    }

    @GetMapping("/all")
    ApiResponse<List<PostingResponse>> getAllPostings() {
        ApiResponse<List<PostingResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.getAllPostings());
        return apiResponse;
    }
}
