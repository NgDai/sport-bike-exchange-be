package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "InspectionReport")
public class InspectionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reportId", nullable = false)
    private Integer reportId;

    @Column(name = "disputeId")
    private Integer disputeId;

    @Column(name = "inspectorId")
    private Integer inspectorId;

    @Column(name = "result")
    private String result;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;
}
