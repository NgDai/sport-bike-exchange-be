package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class InspectionReportResponse {
    private int reportId;
    private int disputeId;
    private int inspectorId;
    private String result;
    private String reason;
    private String note;
    private Date createAt;
}
