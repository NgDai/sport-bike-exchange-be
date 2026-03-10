package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.response.SystemConfigResponse;
import com.bicycle.marketplace.entities.SystemConfig;
import com.bicycle.marketplace.mapper.SystemConfigMapper;
import com.bicycle.marketplace.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SystemConfigService {
    SystemConfigRepository systemConfigRepository;
    SystemConfigMapper systemConfigMapper;

    public SystemConfigResponse getConfigValue(String key) {
        SystemConfig config = systemConfigRepository.findByKey(key)
                .orElseThrow(() -> new RuntimeException("Config key not found: " + key));
        return systemConfigMapper.toSystemConfig(config);

    }

    public SystemConfigResponse setConfigValue(String key, double value) {
        var config = systemConfigRepository.findByKey(key)
                .orElseThrow(() -> new RuntimeException("Config key not found: " + key));
        config.setValue(value);
        systemConfigRepository.save(config);
        return  systemConfigMapper.toSystemConfig(config);
    }
}
