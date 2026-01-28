package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inspection_reports")
public class InspectionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", unique = true, nullable = false)
    private Long reportId;

    @Column(name = "frame_condition")
    private String frameCondition;

    @Column(name = "brake_condition")
    private String brakeCondition;

    @Column(name = "wheel_condition")
    private String wheelCondition;

    @Column(name = "seller_sign")
    private String sellerSign;

    @Column(name = "buyer_sign")
    private String buyerSign;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;
}
