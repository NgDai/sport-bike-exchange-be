package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostingCreationRequest {
    private Integer sellerId;
    private Integer eventId;
    private Integer brandId;
    private Integer categoryId;
    private String title;
    private String model;
    private String frameSize;
    private String wheelSize;
    private Integer manufactureYear;
    private String brakeType;
    private String transmission;
    private Double weight;
    private String imageUrl;
    private String description;
    private Double price;
    private String status;
}
