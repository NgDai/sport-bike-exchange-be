package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.CreatePostingRequest;
import com.bicycle.marketplace.dto.request.UpdatePostingRequest;
import com.bicycle.marketplace.dto.response.PostingResponse;
import com.bicycle.marketplace.entities.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PostingMapper {
    Bicycle toBicycle(CreatePostingRequest request, Brand brand, Category category);

    BikeListing toBikeListing(CreatePostingRequest request, Bicycle bicycle);

    void updateBicycleFromRequest(UpdatePostingRequest request, @MappingTarget Bicycle bicycle);

    void updateBikeListingFromRequest(UpdatePostingRequest request, @MappingTarget BikeListing bikeListing);

    @Mapping(source = "seller.username", target = "sellerName")
    @Mapping(source = "bicycle.bikeId", target = "bikeId")
    @Mapping(source = "bicycle.brand.name", target = "brandName")
    @Mapping(source = "bicycle.category.name", target = "categoryName")
    @Mapping(source = "bicycle.bikeType", target = "bikeType")
    @Mapping(source = "bicycle.wheelSize", target = "wheelSize")
    @Mapping(source = "bicycle.numberOfGears", target = "numberOfGears")
    @Mapping(source = "bicycle.brakeType", target = "brakeType")
    @Mapping(source = "bicycle.yearManufacture", target = "yearManufacture")
    @Mapping(source = "bicycle.frameSize", target = "frameSize")
    @Mapping(source = "bicycle.drivetrain", target = "drivetrain")
    @Mapping(source = "bicycle.forkType", target = "forkType")
    @Mapping(source = "bicycle.color", target = "color")
    @Mapping(source = "bicycle.frameMaterial", target = "frameMaterial")
    @Mapping(source = "bicycle.condition", target = "condition")
    PostingResponse toPostingResponse(BikeListing bikeListing);
}
