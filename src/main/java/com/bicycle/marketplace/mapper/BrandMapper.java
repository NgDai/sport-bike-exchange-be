package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.BrandCreationRequest;
import com.bicycle.marketplace.dto.request.BrandUpdateRequest;
import com.bicycle.marketplace.dto.response.BrandResponse;
import com.bicycle.marketplace.entities.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    Brand toBrand(BrandCreationRequest request);
    BrandResponse toBrandResponse(Brand brand);
    void updateBrand(@MappingTarget Brand brand, BrandUpdateRequest request);
}
