package com.bicycle.marketplace.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreationRequest {
    private String status;
    private double amount;
    private double actualPrice;
    private Date createdAt;
    private Date updateAt;
}
