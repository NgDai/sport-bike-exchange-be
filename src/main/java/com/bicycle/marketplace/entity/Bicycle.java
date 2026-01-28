package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bicycles")
public class Bicycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bike_id", unique = true, nullable = false)
    private Long bikeId;

    @Column(name = "frame_size")
    private String frameSize;

    @Column(name = "brake_type")
    private String brakeType;

    @Column(name = "wheel_size")
    private String wheelSize;

    @Column(name = "weight")
    private Float weight;

    @Column(name = "number_of_gears")
    private Integer numberOfGears;

    @Column(name = "year_manufacture")
    private Integer yearManufacture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToOne(mappedBy = "bicycle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Posting posting;
}
