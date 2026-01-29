package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventId", unique = true, nullable = false)
    private Integer eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createBy")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createBy;

    @Column(name = "name")
    private String name;

    @Column(name = "location")
    private String location;

    @Column(name = "startDate")
    private LocalDateTime startDate;

    @Column(name = "endDate")
    private LocalDateTime endDate;

    @Column(name = "sellerDepositRate")
    private Double sellerDepositRate;

    @Column(name = "buyerDepositRate")
    private Double buyerDepositRate;

    @Column(name = "platformFeeRate")
    private Double platformFeeRate;

    @Column(name = "status")
    private String status;
}
