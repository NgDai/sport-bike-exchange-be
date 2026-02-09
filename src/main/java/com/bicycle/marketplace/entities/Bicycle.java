package com.bicycle.marketplace.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bicycle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int bikeId;
    @ManyToOne
    Brand brandId;
    @ManyToOne
    Category cateId;
    String bikeType;
    String wheelSize;
    String numberOfGear;
    String brakeType;
    LocalDate yearManufactured;
    String frameSize;
    String driveTrain;
    String forkType;
    String color;
    String frameMaterial;
}
