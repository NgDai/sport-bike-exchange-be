package com.bicycle.marketplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
public class CreatePostingRequest {
    String brandName;
    String categoryName;
    @NotBlank
    String title;
    String description;
    float price;
    String bikeType;
    String wheelSize;
    String numberOfGears;
    String brakeType;
    int yearManufacture;
    String frameSize;
    String drivetrain;
    String forkType;
    String color;
    String frameMaterial;
    int condition;
}
