package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "depositId", unique = true, nullable = false)
    private Integer depositId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listingId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BikeListing listing;

    @Column(name = "type")
    private String type;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "status")
    private String status;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;
}
