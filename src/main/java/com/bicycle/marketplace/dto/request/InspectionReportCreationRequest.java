package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class InspectionReportCreationRequest {
    private String result;
    private String reason;
    private String note;
    private Date createAt;
}
