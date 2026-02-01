package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.PostingCreationRequest;
import com.bicycle.marketplace.dto.request.PostingUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.entity.BikeListing;
import com.bicycle.marketplace.service.BikeListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bike-listings")
public class BikeListingController {

    @Autowired
    private BikeListingService bikeListingService;

    @PostMapping
    ApiResponse<BikeListing> createBikeListing(@RequestBody PostingCreationRequest request) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bikeListingService.createBikeListing(request));
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<BikeListing>> getAllBikeListings() {
        ApiResponse<List<BikeListing>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bikeListingService.getAllBikeListings());
        return apiResponse;
    }

    @GetMapping("/{listingId}")
    ApiResponse<BikeListing> getBikeListingById(@PathVariable int listingId) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bikeListingService.getBikeListingById(listingId));
        return apiResponse;
    }

    @PutMapping("/{listingId}")
    ApiResponse<BikeListing> updateBikeListing(@PathVariable int listingId, @RequestBody PostingUpdateRequest request) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bikeListingService.updateBikeListing(listingId, request));
        return apiResponse;
    }

    @DeleteMapping("/{listingId}")
    ApiResponse<String> deleteBikeListing(@PathVariable int listingId) {
        bikeListingService.deleteBikeListing(listingId);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Bike listing deleted successfully");
        return apiResponse;
    }
}
