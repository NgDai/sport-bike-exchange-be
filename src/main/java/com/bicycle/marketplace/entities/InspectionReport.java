package com.bicycle.marketplace.entities;

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
    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = true)
    Reservation reservation;
    @ManyToOne
    @JoinColumn(name = "inspector_id")
    Users inspector;
    String result; // PASS, FAIL, PENDING
    @Column(columnDefinition = "TEXT")
    String checklistItems; // JSON array: [{"name":"...","status":"PASS/FAIL/NOT_CHECKED","note":"..."}]
    @CreationTimestamp
    Date createdAt;
}
