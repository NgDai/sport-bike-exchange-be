package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disputeId", unique = true, nullable = false)
    private Integer disputeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transactionId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raisedBy")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User raisedByUser;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status")
    private String status;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;
}
