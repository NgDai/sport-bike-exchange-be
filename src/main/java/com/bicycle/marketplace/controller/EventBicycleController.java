package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.CreatePostingRequest;
import com.bicycle.marketplace.dto.request.EventBicycleCreationRequest;
import com.bicycle.marketplace.dto.request.EventBicycleUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.EventBicycleResponse;
import com.bicycle.marketplace.entities.Bicycle;
import com.bicycle.marketplace.entities.EventBicycle;
import com.bicycle.marketplace.services.EventBicycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event-bicycles")
public class EventBicycleController {
    @Autowired
    private EventBicycleService eventBicycleService;

    @PostMapping("/event/{eventId}/listing/{listingId}/register")
    ApiResponse<EventBicycleResponse> createEventBicycle(
            @PathVariable int eventId,
            @PathVariable int listingId,
            @RequestBody EventBicycleCreationRequest request) {
        ApiResponse<EventBicycleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventBicycleService.registerBicycleToEvent(eventId, listingId, request));
        apiResponse.setMessage("Event Bicycle created successfully");
        return apiResponse;
    }

    @PostMapping("/bicycle/create")
    ApiResponse<Bicycle> createBicycle(@RequestBody CreatePostingRequest request) {
        ApiResponse<Bicycle> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventBicycleService.createBicycle(request));
        return apiResponse;
    }

    @PostMapping("/event/{eventId}/bicycle/{bicycleId}/register")
    ApiResponse<EventBicycleResponse> createEventBicycleWithoutPosting(
            @PathVariable int eventId,
            @PathVariable int bicycleId,
            @RequestBody EventBicycleCreationRequest request) {
        ApiResponse<EventBicycleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventBicycleService.registerBicycleToEventWithoutPosting(eventId, bicycleId, request));
        apiResponse.setMessage("Event Bicycle registered successfully.");
        return apiResponse;
    }

    @PutMapping("/{eventBikeId}")
    ApiResponse<EventBicycleResponse> updateEventBicycle(@PathVariable int eventBikeId, @RequestBody EventBicycleUpdateRequest request) {
        ApiResponse<EventBicycleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventBicycleService.updateEventBicycle(eventBikeId, request));
        apiResponse.setMessage("Event Bicycle updated successfully");
        return apiResponse;
    }

    @PutMapping("/{eventBikeId}/status")
    ApiResponse<EventBicycleResponse> updateEventBicycleStatus(@PathVariable int eventBikeId) {
        ApiResponse<EventBicycleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventBicycleService.updateEventBicycleStatus(eventBikeId));
        apiResponse.setMessage("Event Bicycle status updated successfully");
        return apiResponse;
    }

    @GetMapping("/{eventBikeId}")
    ApiResponse<EventBicycleResponse> getEventBicycleById(@PathVariable int eventBikeId) {
        ApiResponse<EventBicycleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventBicycleService.getEventBicycleById(eventBikeId));
        apiResponse.setMessage("Event Bicycle fetched successfully");
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<EventBicycle>> getAllEventBicycles() {
        ApiResponse<List<EventBicycle>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventBicycleService.getAllEventBicycles());
        apiResponse.setMessage("Event Bicycles fetched successfully");
        return apiResponse;
    }

    @DeleteMapping("/{eventBikeId}")
    ApiResponse<String> deleteEventBicycle(@PathVariable int eventBikeId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventBicycleService.deleteEventBicycle(eventBikeId));
        return apiResponse;
    }

}
