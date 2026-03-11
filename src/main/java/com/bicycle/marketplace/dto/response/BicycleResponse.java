package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private int yearManufacture;
    private String frameSize;
    private String drivetrain;
    private String forkType;
    private String color;
    private String frameMaterial;

    // Thông số kỹ thuật bổ sung
    private Double weight; // Cân nặng (kg)
    private String saddle; // Yên xe
    private String chainring; // Đĩa
    private String fork; // Phuộc
    private String chain; // Xích xe
    private String handlebar; // Ghi đông
    private String rim; // Vành xe
    private String shockAbsorber; // Giảm xốc
}
