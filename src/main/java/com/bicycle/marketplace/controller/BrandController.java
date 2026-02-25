package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.BrandCreationRequest;
import com.bicycle.marketplace.dto.request.BrandUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.BrandResponse;
import com.bicycle.marketplace.entities.Brand;
import com.bicycle.marketplace.services.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brands")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<BrandResponse> createBrand(@RequestBody BrandCreationRequest request) {
        ApiResponse<BrandResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(brandService.createBrand(request));
        apiResponse.setMessage("Brand created successfully");
        return apiResponse;
    }

    @PutMapping("/{brandId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<BrandResponse> updateBrand(@PathVariable int brandId, @RequestBody BrandUpdateRequest request) {
        ApiResponse<BrandResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(brandService.updateBrand(brandId, request));
        apiResponse.setMessage("Brand updated successfully");
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<Brand>> getAllBrands() {
        ApiResponse<List<Brand>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(brandService.getAllBrands());
        apiResponse.setMessage("Brands fetched successfully");
        return apiResponse;
    }

    @GetMapping("/{brandId}")
    ApiResponse<BrandResponse> getBrandById(@PathVariable int brandId) {
        ApiResponse<BrandResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(brandService.getBrandById(brandId));
        apiResponse.setMessage("Brand fetched successfully");
        return apiResponse;
    }

    @DeleteMapping("/{brandId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deleteBrand(@PathVariable int brandId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(brandService.deleteBrand(brandId));
        return apiResponse;
    }
}
