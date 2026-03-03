package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PostingResponse {
    String title;
    String seller;
    String description;
    int price;
    String brandName;
    String categoryName;
    private String bikeType;
    private String wheelSize;
    private String numberOfGears;
    private String brakeType;
    private LocalDate yearManufacture;
    private String frameSize;
    private String drivetrain;
    private String forkType;
    private String color;
    private String frameMaterial;
    private int condition;
}
