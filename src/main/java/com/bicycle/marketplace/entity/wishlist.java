package com.bicycle.marketplace.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class wishlist {
    private String wishlist_id;
    private String user_id;
    private String post_id;
}
