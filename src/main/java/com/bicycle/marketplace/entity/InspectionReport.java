package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class InspectionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reportId", nullable = false)
    private Integer reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disputeId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Dispute dispute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspectorId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User inspector;

    @Column(name = "result")
    private String result;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;
}
