package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.CheckInCreationRequest;
import com.bicycle.marketplace.dto.request.CheckInUpdateRequest;
import com.bicycle.marketplace.dto.response.CheckInResponse;
import com.bicycle.marketplace.entities.CheckIn;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CheckInMapper {
    CheckInResponse toCheckInResponse(CheckIn checkIn);
    void updateCheckIn(@MappingTarget CheckIn checkIn, CheckInUpdateRequest request);
}
