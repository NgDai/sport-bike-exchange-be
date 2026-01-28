package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ListingApproval")
public class ListingApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approvalId", unique = true, nullable = false)
    private Integer approvalId;

    @Column(name = "listingId")
    private Integer listingId;

    @Column(name = "moderatorId")
    private Integer moderatorId;

    @Column(name = "decision")
    private String decision;

    @Column(name = "note")
    private String note;

    @Column(name = "approvedAt")
    private LocalDateTime approvedAt;
}
