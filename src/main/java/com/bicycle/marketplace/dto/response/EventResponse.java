package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private int eventId;
    private String createBy;
    private String name;
    private String bikeType;
    private String location;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDate createDate;
    private LocalDate startDate;
    private LocalDate publicDate;
    private LocalDate updateTime;
    private LocalDate endDate;
    private String status;
}