package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.CreatePostingRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostingService {
    private IBikeListingRepository bikeListingRepository;
    private PostingMapper postingMapper;
    private IUserRepository userRepository;
    private IBrandRepository brandRepository;
    private ICategoryRepository categoryRepository;

    @Transactional
    public BikeListing createPosting(CreatePostingRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Brand brand = brandRepository.findByNameIgnoreCase(request.getBrandName())
                .orElseThrow();

        Category category = categoryRepository.findByNameIgnoreCase(request.getCategoryName())
                .orElseThrow();

        Bicycle bicycle = postingMapper.toBicycle(request, brand, category);

        BikeListing bikeListing = postingMapper.toBikeListing(request, bicycle);
        bikeListing.setSeller(user);

        return bikeListingRepository.save(bikeListing);
    }
}
