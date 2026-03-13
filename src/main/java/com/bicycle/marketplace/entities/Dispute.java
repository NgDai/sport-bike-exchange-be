package com.bicycle.marketplace.entities;

import com.bicycle.marketplace.enums.DisputeReasonCategory;
import com.bicycle.marketplace.enums.DisputeStatus;
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
@Table(name = "dispute")
public class Dispute {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int disputeId;
    @OneToOne
    @JoinColumn(name = "transaction_id")
    Transaction transaction;
    @ManyToOne
    @JoinColumn(name = "raised_by")
    Users raisedBy;
    /** Mô tả chi tiết lý do (tự do). */
    String reason;
    @Enumerated(EnumType.STRING)
    DisputeStatus status;
    /** Phân loại lý do tranh chấp. */
    @Enumerated(EnumType.STRING)
    DisputeReasonCategory reasonCategory;
    /** Inspector được gán xử lý tranh chấp (nullable). */
    @ManyToOne
    @JoinColumn(name = "assigned_inspector_id")
    Users assignedInspector;
    @CreationTimestamp
    Date createdAt;
}
