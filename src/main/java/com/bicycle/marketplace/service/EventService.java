package com.bicycle.marketplace.service;

import com.bicycle.marketplace.Repository.IEventRepository;
import com.bicycle.marketplace.dto.request.EventCreationRequest;
import com.bicycle.marketplace.entity.Events;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {
    @Autowired
    private IEventRepository eventRepository;

    public Events createEvent(EventCreationRequest request) {
        Events event = new Events();

        event.setName(request.getName());
        event.setLocation(request.getLocation());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setSellerDepositRate(request.getSellerDepositRate());
        event.setBuyerDepositRate(request.getBuyerDepositRate());
        event.setPlatformFeeRate(request.getPlatformFeeRate());
        event.setStatus(request.getStatus());

        return eventRepository.save(event);
    }

    public void updateEvent(int eventId, EventCreationRequest request) {
        Events event = getEventById(eventId);

        event.setName(request.getName());
        event.setLocation(request.getLocation());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setSellerDepositRate(request.getSellerDepositRate());
        event.setBuyerDepositRate(request.getBuyerDepositRate());
        event.setPlatformFeeRate(request.getPlatformFeeRate());
        event.setStatus(request.getStatus());

        eventRepository.save(event);
    }

    public Events updateEventStatus(int eventId, EventCreationRequest request) {
        Events event = getEventById(eventId);
        event.setStatus(request.getStatus());
        return eventRepository.save(event);
    }

    public void deleteEvent(int eventId) {
        Events event = getEventById(eventId);
        eventRepository.delete(event);
    }

    public List<Events> getAllEvents() {
        return eventRepository.findAll();
    }

    public Events getEventById(int eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
    }
}
