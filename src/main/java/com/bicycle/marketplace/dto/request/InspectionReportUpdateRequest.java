package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class InspectionReportUpdateRequest {
    private String result;
    private String reason;
    private String note;
    private Date createAt;
    private List<ChecklistItemRequest> checklistItems;
    private Boolean buyerCheckin;
    private Boolean sellerCheckin;
}
