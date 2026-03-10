package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.SystemConfigResponse;
import com.bicycle.marketplace.services.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fee")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SystemConfigController {
    SystemConfigService systemConfigService;

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
