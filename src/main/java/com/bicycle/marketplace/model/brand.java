package com.bicycle.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class brand {
    private String brand_id;
    private String name;
    private String country;
    private String description;
}
