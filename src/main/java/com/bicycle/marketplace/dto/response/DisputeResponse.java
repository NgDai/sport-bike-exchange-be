package com.bicycle.marketplace.dto.response;

import com.bicycle.marketplace.enums.DisputeReasonCategory;
import com.bicycle.marketplace.enums.DisputeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DisputeResponse {
    private int disputeId;
    private int transactionId;
    private String raisedBy;
    private Integer raisedByUserId;
    private String reason;
    private DisputeStatus status;
    private DisputeReasonCategory reasonCategory;
    private Integer assignedInspectorId;
    private String assignedInspectorName;
    private Date createdAt;
}
