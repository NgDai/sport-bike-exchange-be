package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventCreationRequest {
    private String name;
    private String bikeType;
    private String location;
    private String address;
    private LocalDate createDate;
    private LocalDate startDate;
    private LocalDate publicDate;
    private LocalDate updateTime;
    private LocalDate endDate;
    private String status;
}