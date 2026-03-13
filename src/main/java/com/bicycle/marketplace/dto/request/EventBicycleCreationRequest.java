package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventBicycleCreationRequest {
    private Double price;
    private String title;
    private String bikeType;
    private String condition;
    private String image_url;
    private LocalDate createDate;
}
