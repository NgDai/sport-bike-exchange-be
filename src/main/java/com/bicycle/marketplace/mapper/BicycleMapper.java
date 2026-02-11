package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.BicycleCreationRequest;
import com.bicycle.marketplace.dto.request.BicycleUpdateRequest;
import com.bicycle.marketplace.dto.response.BicycleResponse;
import com.bicycle.marketplace.entities.Bicycle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BicycleMapper {
    BicycleResponse toBicycleResponse(Bicycle bicycle);
    Bicycle toBicycle(BicycleCreationRequest request);
    void updateBicycle(@MappingTarget Bicycle bicycle, BicycleUpdateRequest request);
}
