package com.bicycle.marketplace.dto.request;

import com.bicycle.marketplace.enums.DisputeReasonCategory;
import com.bicycle.marketplace.enums.DisputeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DisputeUpdateRequest {
    private String reason;
    private DisputeStatus status;
    private DisputeReasonCategory reasonCategory;
}
