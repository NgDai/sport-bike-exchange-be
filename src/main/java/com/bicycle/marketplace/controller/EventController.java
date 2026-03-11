// File: src/main/java/com/bicycle/marketplace/controller/EventController.java
package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.EventCreationRequest;
import com.bicycle.marketplace.dto.request.EventUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.EventResponse;
import com.bicycle.marketplace.dto.response.EventInspectorResponse;
import com.bicycle.marketplace.dto.request.AssignInspectorRequest;
import com.bicycle.marketplace.services.EventService;
import com.bicycle.marketplace.services.EventInspectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @Autowired
    private EventInspectorService eventInspectorService;

    @PostMapping("/{eventId}/inspectors")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<EventInspectorResponse> assignInspectorToEvent(
            @PathVariable int eventId,
            @RequestBody AssignInspectorRequest request) {
        ApiResponse<EventInspectorResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventInspectorService.assignEventInspectorToEvent(eventId, request.getInspectorId()));
        apiResponse.setMessage("Inspector assigned to event successfully");
        return apiResponse;
    }

    @GetMapping("/inspectors")
    public ApiResponse<EventInspectorResponse> getAllEventInspectors() {
        ApiResponse<EventInspectorResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventInspectorService.getAllEventInspector());
        apiResponse.setMessage("Event inspectors fetched successfully");
        return apiResponse;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<EventResponse> createEvent(@RequestBody EventCreationRequest request) {
        ApiResponse<EventResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventService.createEvent(request));
        apiResponse.setMessage("Event created successfully");
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<EventResponse>> getAllEvents() {
        ApiResponse<List<EventResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventService.getAllEvents());
        apiResponse.setMessage("Events fetched successfully");
        return apiResponse;
    }

    @GetMapping("/{eventId}")
    ApiResponse<EventResponse> getEventById(@PathVariable int eventId) {
        ApiResponse<EventResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventService.getEventById(eventId));
        apiResponse.setMessage("Event fetched successfully");
        return apiResponse;
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<EventResponse> updateEvent(@PathVariable int eventId, @RequestBody EventUpdateRequest request) {
        ApiResponse<EventResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventService.updateEvent(eventId, request));
        apiResponse.setMessage("Event updated successfully");
        return apiResponse;
    }

    @PutMapping("/status/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<EventResponse> updateEventStatus(@PathVariable int eventId, @RequestBody EventUpdateRequest request) {
        ApiResponse<EventResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventService.updateEventStatus(eventId, request));
        apiResponse.setMessage("Event status updated successfully");
        return apiResponse;
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deleteEvent(@PathVariable int eventId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventService.deleteEvent(eventId));
        return apiResponse;
    }

    @GetMapping("/status/{status}")
    ApiResponse<List<EventResponse>> getEventsByStatus(@PathVariable String status) {
        ApiResponse<List<EventResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventService.getEventsByStatus(status));
        apiResponse.setMessage("Events fetched successfully");
        return apiResponse;
    }
}