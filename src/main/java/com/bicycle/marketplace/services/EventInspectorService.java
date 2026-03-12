package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.AssignInspectorRequest;
import com.bicycle.marketplace.entities.Events;
import com.bicycle.marketplace.repository.IEventInspectorRepository;
import com.bicycle.marketplace.repository.IEventRepository;
import com.bicycle.marketplace.repository.IUserRepository;
import com.bicycle.marketplace.entities.EventInspector;
import com.bicycle.marketplace.dto.response.EventInspectorResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EventInspectorService {
    IEventInspectorRepository eventInspectorRepository;
    IUserRepository userRepository;
    IEventRepository eventRepository;

    public EventInspectorResponse assignEventInspectorToEvent(int eventId, int inpectorId) {
        var inspector = userRepository.findById(inpectorId)
                .orElseThrow(() -> new RuntimeException("Inspector not found with id: " + inpectorId));

        if (inspector.getRole().equalsIgnoreCase("Inspector")) {
            var event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

            EventInspector eventInspector = EventInspector.builder()
                    .event(event)
                    .inspector(inspector)
                    .status("pending")
                    .build();

            eventInspector = eventInspectorRepository.save(eventInspector);

            return EventInspectorResponse.builder()
                    .inspecId(eventInspector.getInspecId())
                    .eventId(event.getEventId())
                    .inspectorId(inspector.getUserId())
                    .inspectorName(inspector.getFullName())
                    .status(eventInspector.getStatus())
                    .createDate(eventInspector.getCreateDate())
                    .build();
        } else {
            throw new RuntimeException("User with id: " + inpectorId + " is not an Inspector");
        }
    }

    public EventInspectorResponse getAllEventInspector() {
        var eventinspector = eventInspectorRepository.findAll();

        return EventInspectorResponse.builder()
                .eventInspectors(eventinspector)
                .build();
    }

    public EventInspectorResponse acceptEvent(int inspecId) {
        var eventInspector = eventInspectorRepository.findById(inspecId)
                .orElseThrow(() -> new RuntimeException("Event Inspector not found with id: " + inspecId));

        eventInspector.setStatus("approved");
        eventInspector = eventInspectorRepository.save(eventInspector);

        return EventInspectorResponse.builder()
                .inspecId(eventInspector.getInspecId())
                .eventId(eventInspector.getEvent().getEventId())
                .inspectorId(eventInspector.getInspector().getUserId())
                .inspectorName(eventInspector.getInspector().getFullName())
                .status(eventInspector.getStatus())
                .createDate(eventInspector.getCreateDate())
                .build();
    }

    public EventInspectorResponse rejectEvent(int inspecId) {
        var eventInspector = eventInspectorRepository.findById(inspecId)
                .orElseThrow(() -> new RuntimeException("Event Inspector not found with id: " + inspecId));

        eventInspector.setStatus("rejected");
        eventInspector = eventInspectorRepository.save(eventInspector);

        return EventInspectorResponse.builder()
                .inspecId(eventInspector.getInspecId())
                .eventId(eventInspector.getEvent().getEventId())
                .inspectorId(eventInspector.getInspector().getUserId())
                .inspectorName(eventInspector.getInspector().getFullName())
                .status(eventInspector.getStatus())
                .createDate(eventInspector.getCreateDate())
                .build();
    }
}
