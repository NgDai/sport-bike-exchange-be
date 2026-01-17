package com.bicycle.marketplace.Entity;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class sections {
    private Integer sectionId;
    private Integer adminId;
    private String name;
    private Date date;
    private String location;
    private String status;
    private Date createdDate;
    private Date updatedDate;
}
