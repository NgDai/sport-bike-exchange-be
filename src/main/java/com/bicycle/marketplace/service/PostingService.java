package com.bicycle.marketplace.service;

import com.bicycle.marketplace.Repository.IBikeListingRepository;
import com.bicycle.marketplace.dto.request.PostingCreationRequest;
import com.bicycle.marketplace.dto.request.PostingUpdateRequest;
import com.bicycle.marketplace.entity.BikeListing;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostingService {
    @Autowired
    private IBikeListingRepository bikeListingRepository;

    public BikeListing createPosting(PostingCreationRequest request) {
        BikeListing listing = new BikeListing();
        listing.setSellerId(request.getSellerId());
        listing.setEventId(request.getEventId());
        listing.setTitle(request.getTitle());
        listing.setBrand(request.getBrand());
        listing.setModel(request.getModel());
        listing.setCategory(request.getCategory());
        listing.setFrameSize(request.getFrameSize());
        listing.setWheelSize(request.getWheelSize());
        listing.setManufactureYear(request.getManufactureYear());
        listing.setBrakeType(request.getBrakeType());
        listing.setTransmission(request.getTransmission());
        listing.setWeight(request.getWeight());
        listing.setImageUrl(request.getImageUrl());
        listing.setDescription(request.getDescription());
        listing.setPrice(request.getPrice());
        listing.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        return bikeListingRepository.save(listing);
    }

    public List<BikeListing> getAllPostings() {
        return bikeListingRepository.findAll();
    }

    public BikeListing getPostingById(Integer listingId) {
        return bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));
    }

    public BikeListing updatePosting(Integer listingId, PostingUpdateRequest request) {
        BikeListing listing = getPostingById(listingId);
        if (request.getEventId() != null) listing.setEventId(request.getEventId());
        if (request.getTitle() != null) listing.setTitle(request.getTitle());
        if (request.getBrand() != null) listing.setBrand(request.getBrand());
        if (request.getModel() != null) listing.setModel(request.getModel());
        if (request.getCategory() != null) listing.setCategory(request.getCategory());
        if (request.getFrameSize() != null) listing.setFrameSize(request.getFrameSize());
        if (request.getWheelSize() != null) listing.setWheelSize(request.getWheelSize());
        if (request.getManufactureYear() != null) listing.setManufactureYear(request.getManufactureYear());
        if (request.getBrakeType() != null) listing.setBrakeType(request.getBrakeType());
        if (request.getTransmission() != null) listing.setTransmission(request.getTransmission());
        if (request.getWeight() != null) listing.setWeight(request.getWeight());
        if (request.getImageUrl() != null) listing.setImageUrl(request.getImageUrl());
        if (request.getDescription() != null) listing.setDescription(request.getDescription());
        if (request.getPrice() != null) listing.setPrice(request.getPrice());
        if (request.getStatus() != null) listing.setStatus(request.getStatus());
        return bikeListingRepository.save(listing);
    }

    public void deletePosting(Integer listingId) {
        BikeListing listing = getPostingById(listingId);
        bikeListingRepository.delete(listing);
    }

    public List<BikeListing> getPostingsBySellerId(Integer sellerId) {
        return bikeListingRepository.findBySellerId(sellerId);
    }

    public List<BikeListing> getPostingsByStatus(String status) {
        return bikeListingRepository.findByStatus(status);
    }
}
