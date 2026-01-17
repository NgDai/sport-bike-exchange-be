package com.bicycle.marketplace.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class posting {
    private String post_id;
    private String seller_id;
    private String bike_id;
    private String name;
    private String description;
    private String condition;
    private double price;
    private String type;
    private String status;
    private String posted_date;
    private String updated_date;
    private double service_fee;
}
