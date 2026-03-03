package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.CreatePostingRequest;
import com.bicycle.marketplace.dto.request.UpdatePostingRequest;
import com.bicycle.marketplace.dto.response.PostingResponse;
import com.bicycle.marketplace.entities.*;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.PostingMapper;
import com.bicycle.marketplace.repository.IBikeListingRepository;
import com.bicycle.marketplace.repository.IBrandRepository;
import com.bicycle.marketplace.repository.ICategoryRepository;
import com.bicycle.marketplace.repository.IUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostingService {
    private final IBikeListingRepository bikeListingRepository;
    private final PostingMapper postingMapper;
    private final IUserRepository userRepository;
    private final IBrandRepository brandRepository;
    private final ICategoryRepository categoryRepository;

    @Transactional
    public BikeListing createPosting(CreatePostingRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Brand brand = brandRepository.findByNameIgnoreCase(request.getBrandName())
                .orElseThrow();

        Category category = categoryRepository.findByNameIgnoreCase(request.getCategoryName())
                .orElseThrow();

        Bicycle bicycle = postingMapper.toBicycle(request, brand, category);

        BikeListing bikeListing = postingMapper.toBikeListing(request, bicycle);
        bikeListing.setSeller(user);

        return bikeListingRepository.save(bikeListing);
    }

    public BikeListing updatePosting(UpdatePostingRequest request, int listingId) {
        BikeListing bikeListing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

        Bicycle bicycle = bikeListing.getBicycle();

        postingMapper.updateBicycleFromRequest(request, bicycle);
        postingMapper.updateBikeListingFromRequest(request, bikeListing);

        return bikeListingRepository.save(bikeListing);
    }

    @Transactional
    public PostingResponse getPostingById(int listingId) {
        BikeListing bikeListing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
        return postingMapper.toPostingResponse(bikeListing);
    }

    @Transactional
    public List<PostingResponse> getAllPostings() {
        return bikeListingRepository.findAll()
                .stream()
                .map(postingMapper::toPostingResponse)
                .collect(Collectors.toList());
    }
}
