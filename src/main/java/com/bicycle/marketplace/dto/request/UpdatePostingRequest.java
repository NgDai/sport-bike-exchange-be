package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePostingRequest {
    // Thông tin của BikeListing
    private String title;
    private String description;
    private double price;
    private String image_url;

    // Tên Brand và Category để tra cứu
    private String brandName;
    private String categoryName;

    // Thông tin của Bicycle
    private String bikeType;
    private String wheelSize;
    private String numberOfGears;
    private String brakeType;
    private int yearManufacture;
    private String frameSize;
    private String drivetrain;
    private String forkType;
    private String color;
    private String frameMaterial;
    private int condition;

    // Các thông số kỹ thuật bổ sung
    private Double weight;
    private String saddle;
    private String chainring;
    private String fork;
    private String chain;
    private String handlebar;
    private String rim;
    private String shockAbsorber;
}