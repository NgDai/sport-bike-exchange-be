package com.bicycle.marketplace.services;

import com.bicycle.marketplace.repository.IEventRepository;
import com.bicycle.marketplace.dto.request.EventCreationRequest;
import com.bicycle.marketplace.dto.request.EventUpdateRequest;
import com.bicycle.marketplace.dto.response.EventResponse;
import com.bicycle.marketplace.entities.Events;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.EventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {
    @Autowired
    private IEventRepository eventRepository;
    @Autowired
    private EventMapper eventMapper;

    public EventResponse createEvent(EventCreationRequest request) {
        Events event = eventMapper.toEvents(request);
        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    public EventResponse updateEvent(int eventId, EventUpdateRequest request) {
        Events event = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        eventMapper.updateEvent(event, request);

        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    public EventResponse updateEventStatus(int eventId, EventCreationRequest request) {
        Events event = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        event.setStatus(request.getStatus());
        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    public String deleteEvent(int eventId) {
        Events event = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        eventRepository.delete(event);
        return "Event deleted successfully";
    }

    public List<Events> getAllEvents() {
        return eventRepository.findAll();
    }

    public EventResponse getEventById(int eventId) {
        Events event = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return eventMapper.toEventResponse(event);
    }
}
