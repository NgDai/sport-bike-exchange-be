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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int listingId;
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Users seller;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Events event;
    private String title;
    private String brand;
    private String model;
    private String category;
    private String frameSize;
    private String wheelSize;
    private Integer manufactureYear;
    private String brakeType;
    private String transmission;
    private Double weight;
    private String imageUrl;
    private String description;
    private Double price;
    private String status;
    private LocalDateTime createdAt;
}