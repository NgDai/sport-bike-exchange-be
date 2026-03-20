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
public class InspectionReportCreationRequest {
    private String result;
    private Date createAt;
    private List<ChecklistItemRequest> checklistItems;
}
