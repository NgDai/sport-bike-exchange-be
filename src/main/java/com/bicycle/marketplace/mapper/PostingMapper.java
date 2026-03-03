package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.BicycleInfoRequest;
import com.bicycle.marketplace.dto.request.CreatePostingRequest;
import com.bicycle.marketplace.entities.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostingMapper {
    Bicycle toBicycle(CreatePostingRequest request, Brand brand, Category category);
    BikeListing toBikeListing(CreatePostingRequest request, Bicycle bicycle);

}
