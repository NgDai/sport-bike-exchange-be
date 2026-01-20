package com.bicycle.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class bicycle {
    private String bike_id;
    private String brand_id;
    private String category_id;
    private String post_id;
    private String frame_size;
    private String brake_type;
    private String wheel_size;
    private String weight;
    private int number_of_gear;
    private int year_manufacture;
}
