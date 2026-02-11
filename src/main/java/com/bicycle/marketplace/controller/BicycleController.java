package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.BicycleCreationRequest;
import com.bicycle.marketplace.dto.request.BicycleUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.BicycleResponse;
import com.bicycle.marketplace.entities.Bicycle;
import com.bicycle.marketplace.services.BicycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bicycles")
public class BicycleController {
    @Autowired
    private BicycleService bicycleService;

    @PostMapping
    ApiResponse<BicycleResponse> createBicycle(@RequestBody BicycleCreationRequest request) {
        ApiResponse<BicycleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bicycleService.createBicycle(request));
        apiResponse.setMessage("Create successfully!");
        return apiResponse;
    }

    @PutMapping("/{bikeId}")
    ApiResponse<BicycleResponse> updateBicycle(@PathVariable int bikeId, @RequestBody BicycleUpdateRequest request) {
        ApiResponse<BicycleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bicycleService.updateBicycle(bikeId, request));
        apiResponse.setMessage("Update successfully!");
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<Bicycle>> getAllBicycles() {
        ApiResponse<List<Bicycle>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bicycleService.findAllBicycles());
        apiResponse.setMessage("Get all bicycles successfully!");
        return apiResponse;
    }

    @GetMapping("/{bikeId}")
    ApiResponse<BicycleResponse> getBicycleById(@PathVariable int bikeId) {
        ApiResponse<BicycleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bicycleService.findBicycleById(bikeId));
        apiResponse.setMessage("Get bicycle by id successfully!");
        return apiResponse;
    }

    @DeleteMapping("/{bikeId}")
    ApiResponse<String> deleteBicycle(@PathVariable int bikeId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(bicycleService.deleteBicycle(bikeId));
        return apiResponse;
    }
}
