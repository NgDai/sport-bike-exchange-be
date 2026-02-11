package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.EventBicycleCreationRequest;
import com.bicycle.marketplace.dto.request.EventBicycleUpdateRequest;
import com.bicycle.marketplace.dto.response.EventBicycleResponse;
import com.bicycle.marketplace.entities.EventBicycle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventBicycleMapper {
    EventBicycle toEventBicycle(EventBicycleCreationRequest request);
    EventBicycleResponse toEventBicycleResponse(EventBicycle eventBicycle);
    void updateEventBicycle(@MappingTarget EventBicycle eventBicycle, EventBicycleUpdateRequest request);
}
