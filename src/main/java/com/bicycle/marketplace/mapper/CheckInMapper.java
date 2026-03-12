package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.CheckInCreationRequest;
import com.bicycle.marketplace.dto.request.CheckInUpdateRequest;
import com.bicycle.marketplace.dto.response.CheckInResponse;
import com.bicycle.marketplace.entities.CheckIn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CheckInMapper {
    @Mapping(source = "buyer.userId", target = "buyerId")
    @Mapping(source = "seller.userId", target = "sellerId")
    @Mapping(source = "event.eventId", target = "eventId")
    CheckInResponse toCheckInResponse(CheckIn checkIn);

    @Mapping(target = "buyer", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "event", ignore = true)
    CheckIn toCheckIn(CheckInCreationRequest request);

    void updateCheckIn(@MappingTarget CheckIn checkIn, CheckInUpdateRequest request);
}
