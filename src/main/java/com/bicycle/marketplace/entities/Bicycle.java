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
    @JoinColumn(name = "brandId")
    Brand brand;
    @ManyToOne
    @JoinColumn(name = "categoryId")
    Category category;
    String bikeType;
    String wheelSize;
    String numberOfGears;
    String brakeType;
    LocalDate yearManufacture;
    String frameSize;
    String drivetrain;
    String forkType;
    String color;
    String frameMaterial;
}
