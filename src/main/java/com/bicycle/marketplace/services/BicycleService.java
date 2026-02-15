package com.bicycle.marketplace.services;

import com.bicycle.marketplace.repository.IBicycleRepository;
import com.bicycle.marketplace.dto.request.BicycleCreationRequest;
import com.bicycle.marketplace.dto.request.BicycleUpdateRequest;
import com.bicycle.marketplace.dto.response.BicycleResponse;
import com.bicycle.marketplace.entities.Bicycle;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.BicycleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BicycleService {
    @Autowired
    private IBicycleRepository bicycleRepository;
    @Autowired
    private BicycleMapper bicycleMapper;

    public BicycleResponse createBicycle(BicycleCreationRequest request) {
        Bicycle bicycle = bicycleMapper.toBicycle(request);
        return bicycleMapper.toBicycleResponse(bicycleRepository.save(bicycle));
    }

    public BicycleResponse updateBicycle(int bikeId, BicycleUpdateRequest request) {
        Bicycle bicycle = bicycleRepository.findById(bikeId)
                .orElseThrow(() -> new AppException(ErrorCode.BICYCLE_NOT_FOUND));
        bicycleMapper.updateBicycle(bicycle, request);
        return bicycleMapper.toBicycleResponse(bicycleRepository.save(bicycle));
    }

    public BicycleResponse findBicycleById(int bikeId) {
        Bicycle bicycle = bicycleRepository.findById(bikeId)
                .orElseThrow(() -> new AppException(ErrorCode.BICYCLE_NOT_FOUND));
        return bicycleMapper.toBicycleResponse(bicycle);
    }

    public List<Bicycle> findAllBicycles() {
        return bicycleRepository.findAll();
    }

    public String deleteBicycle(int bikeId) {
        Bicycle bicycle = bicycleRepository.findById(bikeId)
                .orElseThrow(() -> new AppException(ErrorCode.BICYCLE_NOT_FOUND));
        bicycleRepository.delete(bicycle);
        return "Bicycle deleted successfully";
    }
}
