package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inspections")
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspection_id", unique = true, nullable = false)
    private Long inspectionId;

    @Column(name = "inspected_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date inspectedDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private SectionItem sectionItem;

    @OneToOne(mappedBy = "inspection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InspectionReport inspectionReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
