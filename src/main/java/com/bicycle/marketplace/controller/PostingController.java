package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.PostingCreationRequest;
import com.bicycle.marketplace.dto.request.PostingUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.services.BikeListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/postings")
public class PostingController {

    @Autowired
    private BikeListingService bikeListingService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    ApiResponse<BikeListing> createPosting(@RequestBody PostingCreationRequest request) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bikeListingService.createBikeListing(request));
        return apiResponse;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<java.util.List<BikeListing>> getAllPostings() {
        ApiResponse<java.util.List<BikeListing>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bikeListingService.getAllBikeListings());
        return apiResponse;
    }

    @GetMapping("/{listingId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    ApiResponse<BikeListing> getPostingById(@PathVariable int listingId) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bikeListingService.getBikeListingById(listingId));
        return apiResponse;
    }

    @PutMapping("/{listingId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<BikeListing> updatePosting(@PathVariable int listingId, @RequestBody PostingUpdateRequest request) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bikeListingService.updateBikeListing(listingId, request));
        return apiResponse;
    }

    @DeleteMapping("/{listingId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deletePosting(@PathVariable int listingId) {
        bikeListingService.deleteBikeListing(listingId);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Posting deleted successfully");
        return apiResponse;
    }
}
