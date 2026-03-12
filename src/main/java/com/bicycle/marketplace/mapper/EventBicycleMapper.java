package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.EventBicycleCreationRequest;
import com.bicycle.marketplace.dto.request.EventBicycleUpdateRequest;
import com.bicycle.marketplace.dto.response.EventBicycleResponse;
import com.bicycle.marketplace.entities.EventBicycle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventBicycleMapper {
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "listing", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "bicycle", ignore = true)
    EventBicycle toEventBicycle(EventBicycleCreationRequest request);

    @Mapping(source = "event.eventId", target = "eventId")
    @Mapping(source = "listing.listingId", target = "listingId")
    @Mapping(source = "seller.userId", target = "sellerId")
    @Mapping(source = "bicycle.bikeId", target = "bikeId")
    EventBicycleResponse toEventBicycleResponse(EventBicycle eventBicycle);
    void updateEventBicycle(@MappingTarget EventBicycle eventBicycle, EventBicycleUpdateRequest request);
}
