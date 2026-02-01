package com.bicycle.marketplace.controller;


import com.bicycle.marketplace.dto.request.EventCreationRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.entities.Events;
import com.bicycle.marketplace.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @PostMapping
    ApiResponse<Events> createEvent(@RequestBody EventCreationRequest request) {
        ApiResponse<Events> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventService.createEvent(request));
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<Events>> getAllEvents() {
        ApiResponse<List<Events>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventService.getAllEvents());
        return apiResponse;
    }

    @GetMapping("/{eventId}")
    ApiResponse<Events> getEventById(@PathVariable int eventId) {
        ApiResponse<Events> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventService.getEventById(eventId));
        return apiResponse;
    }

    @PutMapping("/{eventId}")
    ApiResponse<Void> updateEvent(@PathVariable int eventId, @RequestBody EventCreationRequest request) {
        eventService.updateEvent(eventId, request);
        return new ApiResponse<>();
    }

    @PutMapping("/status/{eventId}")
    ApiResponse<Events> updateEventStatus(@PathVariable int eventId, @RequestBody EventCreationRequest request) {
        ApiResponse<Events> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventService.updateEventStatus(eventId, request));
        return apiResponse;
    }

    @DeleteMapping("/{eventId}")
    ApiResponse<String> deleteEvent(@PathVariable int eventId) {
        eventService.deleteEvent(eventId);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Event deleted successfully");
        return apiResponse;
    }
}
