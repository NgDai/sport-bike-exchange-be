package com.bicycle.marketplace.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class category {
    private String category_id;
    private String name;
    private String bicycle_type;
}
