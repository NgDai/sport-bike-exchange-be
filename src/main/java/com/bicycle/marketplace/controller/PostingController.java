package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.PostingCreationRequest;
import com.bicycle.marketplace.dto.request.PostingUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.services.BikeListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/postings")
public class PostingController {

    @Autowired
    private BikeListingService  bikeListingService;

    @PostMapping
    ApiResponse<BikeListing> createPosting(@RequestBody PostingCreationRequest request) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bikeListingService.createBikeListing(request));
        return apiResponse;
    }

    @GetMapping
    ApiResponse<java.util.List<BikeListing>> getAllPostings() {
        ApiResponse<java.util.List<BikeListing>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bikeListingService.getAllBikeListings());
        return apiResponse;
    }

    @GetMapping("/{listingId}")
    ApiResponse<BikeListing> getPostingById(@PathVariable int listingId) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bikeListingService.getBikeListingById(listingId));
        return apiResponse;
    }

    @PutMapping("/{listingId}")
    ApiResponse<BikeListing> updatePosting(@PathVariable int listingId, @RequestBody PostingUpdateRequest request) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bikeListingService.updateBikeListing(listingId, request));
        return apiResponse;
    }

    @DeleteMapping("/{listingId}")
    ApiResponse<String> deletePosting(@PathVariable int listingId) {
        bikeListingService.deleteBikeListing(listingId);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Posting deleted successfully");
        return apiResponse;
    }
}
