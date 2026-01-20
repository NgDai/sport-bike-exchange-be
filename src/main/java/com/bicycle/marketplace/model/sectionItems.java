package com.bicycle.marketplace.Entity;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter

public class sectionItems {
    private String sectionItemID;
    private String sectionID;
    private String orderID;
    private String buyerID;
    private String sellerID;
    private boolean sellerCheckedIn;
    private String sellerCheckinTime;
    private String buyerCheckedIn;
    private String buyerCheckinTime;
}
