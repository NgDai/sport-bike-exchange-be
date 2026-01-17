package com.bicycle.marketplace.Entity;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class inspectionReports {
    private String reportID;
    private String title;
    private String specID;
    private String sectionItemID;
    private String brakeCondition;
    private String wheelCondition;
    private String frameCondition;
    private boolean isVerified;
    private Date date;
}
