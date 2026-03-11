package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.EventInspectorResponse;
import com.bicycle.marketplace.services.EventInspectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inspector")
public class InspectorEventController {
    @Autowired
    private EventInspectorService eventInspectorService;

    @PutMapping("/acceptEvent/{inspecId}")
    public ApiResponse<EventInspectorResponse> acceptInspectionEvent(@PathVariable int inspecId){

        ApiResponse<EventInspectorResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventInspectorService.acceptEvent(inspecId));
        return apiResponse;
    }

    @PutMapping("/rejectEvent/{inspecId}")
    public ApiResponse<EventInspectorResponse> rejectInspectionEvent(@PathVariable int inspecId){

        ApiResponse<EventInspectorResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(eventInspectorService.rejectEvent(inspecId));
        return apiResponse;
    }
}
