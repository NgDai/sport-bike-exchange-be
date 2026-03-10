package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.response.SystemConfigResponse;
import com.bicycle.marketplace.entities.SystemConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SystemConfigMapper {
    SystemConfigResponse toSystemConfig(SystemConfig value);
}
