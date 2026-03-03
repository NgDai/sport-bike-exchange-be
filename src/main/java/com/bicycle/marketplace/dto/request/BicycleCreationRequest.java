package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BicycleCreationRequest {
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
}
