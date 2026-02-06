package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DepositSettlementResponse {
    private int settlementId;
    private int depositId;
    private int receiverId;
    private double depositAmount;
    private String reason;
    private Date createdAt;
}
