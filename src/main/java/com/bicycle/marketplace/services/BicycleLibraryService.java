package com.bicycle.marketplace.services;

import com.bicycle.marketplace.entities.BicycleLibrary;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.repository.IBicycleLibraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BicycleLibraryService {
    IBicycleLibraryRepository bicycleLibraryRepository;

    public List<BicycleLibrary> getAll() {
        return bicycleLibraryRepository.findAll();
    }

    public List<BicycleLibrary> getByBrandName(String brandName) {
        return bicycleLibraryRepository.findByBrandName(brandName);
    }

    public List<BicycleLibrary> getByBrandNameAndYear(String brandName, int year) {
        return bicycleLibraryRepository.findByBrandNameAndYear(brandName, year);
    }

    public BicycleLibrary getById(int id) {
        return bicycleLibraryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
    }
}
