package com.bicycle.marketplace.services;

import com.bicycle.marketplace.repository.IEventBicycleRepository;
import com.bicycle.marketplace.dto.request.EventBicycleCreationRequest;
import com.bicycle.marketplace.dto.request.EventBicycleUpdateRequest;
import com.bicycle.marketplace.dto.response.EventBicycleResponse;
import com.bicycle.marketplace.entities.EventBicycle;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.EventBicycleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventBicycleService {
    @Autowired
    private IEventBicycleRepository eventBicycleRepository;
    @Autowired
    private EventBicycleMapper eventBicycleMapper;

    public EventBicycleResponse createEventBicycle(EventBicycleCreationRequest request) {
        EventBicycle eventBicycle = eventBicycleMapper.toEventBicycle(request);
        return eventBicycleMapper.toEventBicycleResponse(eventBicycleRepository.save(eventBicycle));
    }

    public EventBicycleResponse updateEventBicycle(int eventBikeId, EventBicycleUpdateRequest request) {
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId).orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
        eventBicycleMapper.updateEventBicycle(eventBicycle, request);
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
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId).orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
        eventBicycleRepository.delete(eventBicycle);
        return "Event Bicycle deleted successfully";
    }
}
