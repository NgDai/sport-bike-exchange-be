package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ListingApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approvalId", unique = true, nullable = false)
    private Integer approvalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listingId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BikeListing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderatorId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User moderator;

    @Column(name = "decision")
    private String decision;

    @Column(name = "note")
    private String note;

    @Column(name = "approvedAt")
    private LocalDateTime approvedAt;
}
