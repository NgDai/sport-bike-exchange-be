package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.InspectorTaskResponse;
import com.bicycle.marketplace.services.InspectorTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inspector/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
public class InspectorTaskController {

    private final InspectorTaskService inspectorTaskService;

    @GetMapping
    public ApiResponse<List<InspectorTaskResponse>> getMyTasks() {
        return ApiResponse.<List<InspectorTaskResponse>>builder()
                .result(inspectorTaskService.getMyTasks())
                .message("Lấy danh sách nhiệm vụ thành công")
                .build();
    }

    @GetMapping("/{taskId}")
    public ApiResponse<InspectorTaskResponse> getTaskById(@PathVariable int taskId) {
        return ApiResponse.<InspectorTaskResponse>builder()
                .result(inspectorTaskService.getTaskById(taskId))
                .message("Lấy chi tiết nhiệm vụ thành công")
                .build();
    }
}