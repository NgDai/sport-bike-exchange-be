package com.bicycle.marketplace.services;

import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Events;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.repository.*;
import com.bicycle.marketplace.dto.request.EventBicycleCreationRequest;
import com.bicycle.marketplace.dto.request.EventBicycleUpdateRequest;
import com.bicycle.marketplace.dto.response.EventBicycleResponse;
import com.bicycle.marketplace.entities.EventBicycle;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.EventBicycleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventBicycleService {
    @Autowired
    private IEventBicycleRepository eventBicycleRepository;
    @Autowired
    private EventBicycleMapper eventBicycleMapper;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IEventRepository eventRepository;
    @Autowired
    private IBikeListingRepository bikeListingRepository;
    @Autowired
    private IBicycleRepository bicycleRepository;

    public EventBicycleResponse registerBicycleToEvent(int eventId, int listingId, EventBicycleCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Events events = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        BikeListing bikeListing = bikeListingRepository.findById(listingId).orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
        EventBicycle eventBicycle = eventBicycleMapper.toEventBicycle(request);
        eventBicycle.setSeller(user);
        eventBicycle.setEvent(events);
        eventBicycle.setListing(bikeListing);
        eventBicycle.setSellerName(username);
        eventBicycle.setStatus("Pending");
        return eventBicycleMapper.toEventBicycleResponse(eventBicycleRepository.save(eventBicycle));
    }

//    public EventBicycleResponse registerBicycleToEventWithoutPosting(int eventId, int bicycleId, EventBicycleCreationRequest request) {}

    public EventBicycleResponse updateEventBicycle(int eventBikeId, EventBicycleUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId).orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
        eventBicycleMapper.updateEventBicycle(eventBicycle, request);
        eventBicycle.setStatus("Pending");
        return eventBicycleMapper.toEventBicycleResponse(eventBicycleRepository.save(eventBicycle));
    }

    public EventBicycleResponse updateEventBicycleStatus(int eventBikeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId).orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
        eventBicycle.setStatus("Available");
        return eventBicycleMapper.toEventBicycleResponse(eventBicycleRepository.save(eventBicycle));
    }

    public EventBicycleResponse getEventBicycleById(int eventBikeId) {
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId).orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
        return eventBicycleMapper.toEventBicycleResponse(eventBicycle);
    }

    public List<EventBicycle> getAllEventBicycles() {
        return eventBicycleRepository.findAll();
    }

    public String deleteEventBicycle(int eventBikeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId).orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
        eventBicycleRepository.delete(eventBicycle);
        return "Event Bicycle deleted successfully";
    }
}
