package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class BikeListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listingId", unique = true, nullable = false)
    private Integer listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sellerId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Event event;

    @Column(name = "title")
    private String title;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "category")
    private String category;

    @Column(name = "frameSize")
    private String frameSize;

    @Column(name = "wheelSize")
    private String wheelSize;

    @Column(name = "manufactureYear")
    private Integer manufactureYear;

    @Column(name = "brakeType")
    private String brakeType;

    @Column(name = "transmission")
    private String transmission;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price")
    private Double price;

    @Column(name = "status")
    private String status;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;
}
