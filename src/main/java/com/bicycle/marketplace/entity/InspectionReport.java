package com.bicycle.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class InspectionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int reportId;
    @OneToOne
    @JoinColumn(name = "dispute_id")
    Dispute dispute;

    @ManyToOne
    @JoinColumn(name = "inspector_id")
    Users inspector;
    String result;
    String reason;
    String note;
    @CreationTimestamp
    Date createAt;
}
