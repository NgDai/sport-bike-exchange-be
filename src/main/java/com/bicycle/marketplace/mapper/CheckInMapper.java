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
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "event.eventId", target = "eventId")
    @Mapping(source = "role", target = "role")
    CheckInResponse toCheckInResponse(CheckIn checkIn);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "event", ignore = true)
    CheckIn toCheckIn(CheckInCreationRequest request);

    void updateCheckIn(@MappingTarget CheckIn checkIn, CheckInUpdateRequest request);
}
