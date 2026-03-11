package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.SystemConfigRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.SystemConfigResponse;
import com.bicycle.marketplace.services.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fee")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SystemConfigController {
    SystemConfigService systemConfigService;

    @PostMapping("/create")
    ApiResponse<SystemConfigResponse> createSystemConfig(@RequestBody SystemConfigRequest request) {
        ApiResponse<SystemConfigResponse> response = new ApiResponse<>();
        response.setResult(systemConfigService.createConfig(request.getConfigKey(), request.getConfigValue()));
        response.setMessage("Tạo cấu hình hệ thống thành công");
        return response;
    }

    @GetMapping("/all")
    ApiResponse<List<SystemConfigResponse>> getAllSystemConfigs() {
        ApiResponse<List<SystemConfigResponse>> response = new ApiResponse<>();
        response.setResult(systemConfigService.getAllConfigs());
        response.setMessage("Lấy tất cả cấu hình hệ thống thành công");
        return response;
    }

    @GetMapping("/{configKey}")
    ApiResponse<SystemConfigResponse> getSystemConfig(@PathVariable String configKey) {
        ApiResponse<SystemConfigResponse> response = new ApiResponse<>();
        response.setResult(systemConfigService.getConfigValue(configKey));
        response.setMessage("Lấy cấu hình hệ thống thành công");
        return response;
    }

    @PutMapping("/{configKey}")
    ApiResponse<SystemConfigResponse> updateSystemConfig(@PathVariable String configKey, @RequestParam double configValue) {
        ApiResponse<SystemConfigResponse> response = new ApiResponse<>();
        response.setResult(systemConfigService.setConfigValue(configKey, configValue));
        response.setMessage("Cập nhật cấu hình hệ thống thành công");
        return response;
    }
}
