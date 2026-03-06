package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BicycleUpdateRequest {
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

    // Thông số kỹ thuật bổ sung
    private Double weight;
    private String saddle;
    private String chainring;
    private String fork;
    private String chain;
    private String handlebar;
    private String rim;
    private String shockAbsorber;
}
