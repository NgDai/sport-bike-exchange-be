// File: src/main/java/com/bicycle/marketplace/mapper/EventMapper.java
package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.EventCreationRequest;
import com.bicycle.marketplace.dto.request.EventUpdateRequest;
import com.bicycle.marketplace.dto.response.EventResponse;
import com.bicycle.marketplace.entities.Events;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(source = "creator.username", target = "createBy")
    EventResponse toEventResponse(Events event);

    @Mapping(target = "creator", ignore = true)
    Events toEvents(EventCreationRequest request);

    void updateEvent(@MappingTarget Events event, EventUpdateRequest request);

    List<EventResponse> toEventResponseList(List<Events> events);
}