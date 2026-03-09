package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.CheckInCreationRequest;
import com.bicycle.marketplace.dto.request.CheckInUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.CheckInResponse;
import com.bicycle.marketplace.entities.CheckIn;
import com.bicycle.marketplace.services.CheckInService;
import com.bicycle.marketplace.services.QRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/checkin")
public class CheckInController {
    @Autowired
    private CheckInService checkInService;
    @Autowired
    private QRService qrService;

    @PostMapping
    ApiResponse<CheckInResponse> createCheckIn(@RequestBody CheckInCreationRequest request){
        ApiResponse<CheckInResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(checkInService.createCheckIn(request));
        apiResponse.setMessage("Check-In created successfully");
        return apiResponse;
    }

    @PutMapping("/{checkInId}")
    ApiResponse<CheckInResponse> updateCheckIn(@PathVariable int checkInId, @RequestBody CheckInUpdateRequest request){
        ApiResponse<CheckInResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(checkInService.updateCheckIn(checkInId, request));
        apiResponse.setMessage("Check-In updated successfully");
        return apiResponse;
    }

    @GetMapping("/{checkInId}")
    ApiResponse<CheckInResponse> getCheckInById(@PathVariable int checkInId){
        ApiResponse<CheckInResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(checkInService.findCheckInById(checkInId));
        apiResponse.setMessage("Check-In fetched successfully");
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<CheckIn>> getAllCheckIns(){
        ApiResponse<List<CheckIn>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(checkInService.findAllCheckIns());
        apiResponse.setMessage("Check-Ins fetched successfully");
        return apiResponse;
    }

    @DeleteMapping("/{checkInId}")
    ApiResponse<String> deleteCheckIn(@PathVariable int checkInId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(checkInService.deleteCheckIn(checkInId));
        return apiResponse;
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<CheckIn>> getCheckInsByStatus(@PathVariable String status) {
        ApiResponse<List<CheckIn>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(checkInService.findCheckInsByStatus(status));
        apiResponse.setMessage("Check-Ins fetched successfully");
        return apiResponse;
    }

    @PostMapping(value = "/qr/create/{checkInId}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] createCheckInQR(@PathVariable int checkInId) throws Exception {
        return checkInService.createCheckInQRCode(checkInId);
    }
//    ApiResponse<byte[]> createCheckInQR(@PathVariable int checkInId) throws Exception {
//        ApiResponse<byte[]> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(checkInService.createCheckInQRCode(checkInId));
//        return apiResponse;
//    }

    @GetMapping("/qr/info/{token}")
    ApiResponse<CheckIn> getInfoFromQR(@PathVariable String token) {
        ApiResponse<CheckIn> apiResponse = new ApiResponse<>();
        apiResponse.setResult(checkInService.getInfoFromQRCode(token));
        apiResponse.setMessage("Check-In info fetched successfully");
        return apiResponse;
    }
}
