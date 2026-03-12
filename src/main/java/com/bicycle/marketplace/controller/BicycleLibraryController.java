package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.entities.BicycleLibrary;
import com.bicycle.marketplace.services.BicycleLibraryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bicycle-library")
public class BicycleLibraryController {

    private final BicycleLibraryService bicycleLibraryService;

    public BicycleLibraryController(BicycleLibraryService bicycleLibraryService) {
        this.bicycleLibraryService = bicycleLibraryService;
    }

    @GetMapping
    ApiResponse<List<BicycleLibrary>> getLibrary(
            @RequestParam(required = false) String brandName,
            @RequestParam(required = false) Integer year) {

        ApiResponse<List<BicycleLibrary>> response = new ApiResponse<>();
        List<BicycleLibrary> result;

        if (brandName != null && year != null) {
            result = bicycleLibraryService.getByBrandNameAndYear(brandName, year);
            response.setMessage("Xe đạp được tìm theo bởi brand và năm");
        } else if (brandName != null) {
            result = bicycleLibraryService.getByBrandName(brandName);
            response.setMessage("Xe đạp được tìm theo bởi brand");
        } else {
            result = bicycleLibraryService.getAll();
            response.setMessage("Tất cả xe đạp tồn tại");
        }

        response.setResult(result);
        return response;
    }

    @GetMapping("/{id}")
    ApiResponse<BicycleLibrary> getById(@PathVariable int id) {
        ApiResponse<BicycleLibrary> response = new ApiResponse<>();
        response.setResult(bicycleLibraryService.getById(id));
        response.setMessage("Nhập thông tin xe đạp thành công");
        return response;
    }
}
