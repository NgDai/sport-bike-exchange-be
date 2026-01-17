package com.bicycle.marketplace.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class orders {
    private String order_id;
    private String order_code;
    private String buyer_id;
    private String note;
    private String deposit_amount;
    private String status;
    private double total_price;
    private String date;
}
