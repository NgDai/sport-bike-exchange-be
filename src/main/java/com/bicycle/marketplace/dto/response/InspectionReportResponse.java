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
    private Integer reservationId;
    private int inspectorId;
    private String result;
    private String checklistItems; // JSON string
    private Date createAt;
    private String buyerName;
    private String sellerName;
}
