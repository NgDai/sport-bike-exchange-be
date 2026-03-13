package com.bicycle.marketplace.dto.request;

import com.bicycle.marketplace.enums.InspectionResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InspectionReportUpdateRequest {
    private InspectionResult result;
    private String reason;
    private String note;
}
