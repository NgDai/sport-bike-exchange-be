package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.CreatePostingRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.PostingResponse;
import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.services.PostingService;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Builder
public class PostingController {

    @Autowired
    PostingService postingService;
    @PostMapping("/create")
    ApiResponse<BikeListing> createPosting(@RequestBody CreatePostingRequest request) {
        ApiResponse<BikeListing>  apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.createPosting(request));
        return  apiResponse;
    }
}
