package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.PostingCreationRequest;
import com.bicycle.marketplace.dto.request.PostingUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.entity.BikeListing;
import com.bicycle.marketplace.service.PostingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/postings")
public class PostingController {
    @Autowired
    private PostingService postingService;

    @PostMapping
    ApiResponse<BikeListing> createPosting(@RequestBody @Valid PostingCreationRequest request) {
        ApiResponse<BikeListing> apiResponse = new ApiResponse<>();
        apiResponse.setResult(postingService.createPosting(request));
        apiResponse.setMessage("Posting created successfully");
        return apiResponse;
    }

    @GetMapping
    List<BikeListing> getAllPostings() {
        return postingService.getAllPostings();
    }

    @GetMapping("/{listingId}")
    BikeListing getPostingById(@PathVariable Integer listingId) {
        return postingService.getPostingById(listingId);
    }

    @PutMapping("/{listingId}")
    BikeListing updatePosting(@PathVariable Integer listingId, @RequestBody PostingUpdateRequest request) {
        return postingService.updatePosting(listingId, request);
    }

    @DeleteMapping("/{listingId}")
    String deletePosting(@PathVariable Integer listingId) {
        postingService.deletePosting(listingId);
        return "Posting deleted successfully";
    }

    @GetMapping("/seller/{sellerId}")
    List<BikeListing> getPostingsBySeller(@PathVariable Integer sellerId) {
        return postingService.getPostingsBySellerId(sellerId);
    }

    @GetMapping("/status/{status}")
    List<BikeListing> getPostingsByStatus(@PathVariable String status) {
        return postingService.getPostingsByStatus(status);
    }
}
