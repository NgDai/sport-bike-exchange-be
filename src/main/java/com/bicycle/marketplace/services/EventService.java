// File: src/main/java/com/bicycle/marketplace/services/EventService.java
package com.bicycle.marketplace.services;

import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.repository.IEventBicycleRepository;
import com.bicycle.marketplace.repository.IEventRepository;
import com.bicycle.marketplace.dto.request.EventCreationRequest;
import com.bicycle.marketplace.dto.request.EventUpdateRequest;
import com.bicycle.marketplace.dto.response.EventResponse;
import com.bicycle.marketplace.entities.Events;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.EventMapper;
import com.bicycle.marketplace.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventService {
    @Autowired
    private IEventRepository eventRepository;
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private IEventBicycleRepository eventBicycleRepository;

    private String computeStatusFromDates(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) {
            return "upcoming";
        } else if (!today.isAfter(endDate)) {
            return "ongoing";
        } else {
            return "completed";
        }
    }

    @Transactional
    public EventResponse createEvent(EventCreationRequest request) {
        Events event = eventMapper.toEvents(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        event.setCreator(user);
        event.setStatus(computeStatusFromDates(event.getStartDate(), event.getEndDate()));

        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    private void updateEventStatusBasedOnDate(Events event) {
        if ("cancelled".equals(event.getStatus())) {
            return;
        }
        String newStatus = computeStatusFromDates(event.getStartDate(), event.getEndDate());
        if (!newStatus.equals(event.getStatus())) {
            event.setStatus(newStatus);
            eventRepository.save(event);
        }
    }


    @Transactional
    public EventResponse updateEvent(int eventId, EventUpdateRequest request) {
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Cập nhật các trường khác (tên, loại xe, địa điểm, ngày...)
        eventMapper.updateEvent(event, request);

        // Xử lý status: nếu request yêu cầu hủy thì set cancelled, ngược lại tự động tính
        if ("cancelled".equalsIgnoreCase(request.getStatus())) {
            event.setStatus("cancelled");
        } else {
            event.setStatus(computeStatusFromDates(event.getStartDate(), event.getEndDate()));
        }

        return eventMapper.toEventResponse(eventRepository.save(event));
    }

//    public EventResponse updateEvent(int eventId, EventUpdateRequest request) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }
//        Events event = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
//        eventMapper.updateEvent(event, request);
//        return eventMapper.toEventResponse(eventRepository.save(event));
//    }

    public EventResponse updateEventStatus(int eventId, EventUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Events event = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        event.setStatus(request.getStatus());
        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse cancelEvent(int eventId) {
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        event.setStatus("cancelled");
        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    @Transactional
    public String deleteEvent(int eventId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Events event = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        if (eventBicycleRepository.existsByEvent_EventId(eventId)) {
            eventBicycleRepository.deleteByEvent_EventId(eventId);
        }
        eventRepository.delete(event);
        return "Event deleted successfully";
    }

    public List<EventResponse> getAllEvents() {
        List<Events> events = eventRepository.findAll();
        events.forEach(this::updateEventStatusBasedOnDate);
        return eventMapper.toEventResponseList(events);
    }

//    public EventResponse getEventById(int eventId) {
//        Events event = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
//        return eventMapper.toEventResponse(event);
//    }

    public EventResponse getEventById(int eventId) {
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        updateEventStatusBasedOnDate(event);
        return eventMapper.toEventResponse(event);
    }

//    public List<EventResponse> getEventsByStatus(String status) {
//        return eventMapper.toEventResponseList(eventRepository.findAllByStatus(status));
//    }

    public List<EventResponse> getEventsByStatus(String status) {
        List<Events> allEvents = eventRepository.findAll();
        allEvents.forEach(this::updateEventStatusBasedOnDate);

        List<Events> filtered = allEvents.stream()
                .filter(e -> e.getStatus().equals(status))
                .toList();
        return eventMapper.toEventResponseList(filtered);
    }


}