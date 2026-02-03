package com.bicycle.marketplace.services;

import com.bicycle.marketplace.Repository.IBikeListingRepository;
import com.bicycle.marketplace.Repository.IEventRepository;
import com.bicycle.marketplace.Repository.IUserRepository;
import com.bicycle.marketplace.dto.request.PostingCreationRequest;
import com.bicycle.marketplace.dto.request.PostingUpdateRequest;
import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Events;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BikeListingService {

    @Autowired
    private IBikeListingRepository bikeListingRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IEventRepository eventRepository;

    public BikeListing createBikeListing(PostingCreationRequest request) {
        if (request.getSellerId() == null) {
            throw new AppException(ErrorCode.POSTING_SELLER_ID_REQUIRED);
        }
        if (request.getEventId() == null) {
            throw new AppException(ErrorCode.POSTING_EVENT_ID_REQUIRED);
        }
        Users seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Events event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        BikeListing listing = new BikeListing();
        listing.setSeller(seller);
        listing.setEvent(event);
        listing.setCreatedAt(LocalDateTime.now());
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
        listing.setStatus(request.getStatus());

        return bikeListingRepository.save(listing);
    }

    public BikeListing updateBikeListing(int listingId, PostingUpdateRequest request) {
        BikeListing listing = getBikeListingById(listingId);

        if (request.getSellerId() != null) {
            Users seller = userRepository.findById(request.getSellerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            listing.setSeller(seller);
        }
        if (request.getEventId() != null) {
            Events event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
            listing.setEvent(event);
        }

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

    public void deleteBikeListing(int listingId) {
        BikeListing listing = getBikeListingById(listingId);
        bikeListingRepository.delete(listing);
    }

    public List<BikeListing> getAllBikeListings() {
        return bikeListingRepository.findAll();
    }

    public BikeListing getBikeListingById(int listingId) {
        return bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));
    }
}
