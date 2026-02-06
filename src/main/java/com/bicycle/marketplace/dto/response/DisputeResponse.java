package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DisputeResponse {
    private int disputeId;
    private int transactionId;
    private int userId;
    private String reason;
    private String status;
    private Date createAt;
}
