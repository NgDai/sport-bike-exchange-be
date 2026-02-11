package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class BicycleUpdateRequest {
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
}
