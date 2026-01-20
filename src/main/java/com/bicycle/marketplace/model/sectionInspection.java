package com.bicycle.marketplace.Entity;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class sectionInspection {
    private String specID;
    private String inspectorID;
    private String status;
    private String summary;
    private Date date;
}
