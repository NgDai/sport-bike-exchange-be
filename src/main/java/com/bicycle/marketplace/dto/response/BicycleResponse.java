package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BicycleResponse {
    private int bikeId;
    private int brandId;
    private int categoryId;
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
