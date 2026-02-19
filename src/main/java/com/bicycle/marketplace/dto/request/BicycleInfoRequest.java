package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO dùng khi nhập thông tin xe đạp cho bài đăng (BikeListing).
 * brandId, categoryId lấy từ API GET /brands và GET /categories.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BicycleInfoRequest {
    private Integer brandId;
    private Integer categoryId;
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
