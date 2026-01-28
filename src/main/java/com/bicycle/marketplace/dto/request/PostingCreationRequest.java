package com.bicycle.marketplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostingCreationRequest {
    private Integer sellerId;
    private Integer eventId;
    @NotBlank(message = "Title is required")
    private String title;
    private String brand;
    private String model;
    private String category;
    private String frameSize;
    private String wheelSize;
    private Integer manufactureYear;
    private String brakeType;
    private String transmission;
    private Double weight;
    private String imageUrl;
    private String description;
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
    private String status;
}
