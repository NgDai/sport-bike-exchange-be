package com.bicycle.marketplace.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class order_detail {
    private String order_detail_id;
    private String order_id;
    private String post_id;
    private int quantity;
}
