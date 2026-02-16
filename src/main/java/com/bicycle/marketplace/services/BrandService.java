package com.bicycle.marketplace.services;

import com.bicycle.marketplace.repository.IBrandRepository;
import com.bicycle.marketplace.dto.request.BrandCreationRequest;
import com.bicycle.marketplace.dto.request.BrandUpdateRequest;
import com.bicycle.marketplace.dto.response.BrandResponse;
import com.bicycle.marketplace.entities.Brand;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.BrandMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private IBrandRepository brandRepository;
    @Autowired
    private BrandMapper brandMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponse createBrand(BrandCreationRequest request){
        Brand brand = brandMapper.toBrand(request);
        return brandMapper.toBrandResponse(brandRepository.save(brand));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponse updateBrand(int brandId, BrandUpdateRequest request){
        Brand brand = brandRepository.findById(brandId).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        brandMapper.updateBrand(brand, request);
        return brandMapper.toBrandResponse(brandRepository.save(brand));
    }

    public List<Brand> getAllBrands(){
        return brandRepository.findAll();
    }

    public BrandResponse getBrandById(int brandId){
        Brand brand = brandRepository.findById(brandId).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        return brandMapper.toBrandResponse(brand);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String deleteBrand(int brandId){
        Brand brand = brandRepository.findById(brandId).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        brandRepository.delete(brand);
        return "Brand deleted successfully";
    }
}
