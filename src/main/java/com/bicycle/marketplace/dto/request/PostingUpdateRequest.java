package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostingUpdateRequest {
    private Integer eventId;
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
    private Double price;
    private String status;
}
