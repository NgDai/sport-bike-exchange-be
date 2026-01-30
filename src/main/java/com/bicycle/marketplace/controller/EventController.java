package com.bicycle.marketplace.controller;


import com.bicycle.marketplace.dto.request.EventCreationRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.entity.Events;
import com.bicycle.marketplace.service.EventService;
import com.bicycle.marketplace.service.UserService;
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
        apiResponse.setMessage("Event created successfully");
        return apiResponse;
    }


    @GetMapping
    List<Events> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{eventId}")
    Events getEventById(@PathVariable int eventId) {
        return eventService.getEventById(eventId);
    }

    @PutMapping("/{eventId}")
    void updateEvent(@PathVariable int eventId, @RequestBody EventCreationRequest request) {
        eventService.updateEvent(eventId, request);
    }

    @PutMapping("/status/{eventId}")
    Events updateEventStatus(@PathVariable int eventId, @RequestBody EventCreationRequest request) {
        return eventService.updateEventStatus(eventId, request);
    }

    @DeleteMapping("/{eventId}")
    String deleteEvent(@PathVariable int eventId) {
        eventService.deleteEvent(eventId);
        return "Event deleted successfully";
    }
}
