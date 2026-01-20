package com.bicycle.marketplace.Entity;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class result {
    private String resultID;
    private String reportID;
    private String sectionItemID;
    private String postID;
    private boolean sellerAttended;
    private boolean buyerAttended;
    private String bicycleResult;
    private float depositAmount;
    private String status;
    private float platformFee;
}
