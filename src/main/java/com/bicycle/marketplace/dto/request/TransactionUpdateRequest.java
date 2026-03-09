package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionUpdateRequest {
    private String status;
    private double amount;
    private double actualPrice;
    private Date createdAt;
    private Date updateAt;
}
