package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionUpdateRequest {
    private String status;
    private double amount;
    private double actualPrice;
    private Date createdAt;
    private Date updateAt;
}
