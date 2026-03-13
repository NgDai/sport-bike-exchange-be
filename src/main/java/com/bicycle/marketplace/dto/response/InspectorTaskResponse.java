package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectorTaskResponse {
    private int id;
    private String bikeName;
    private String bikeImage;
    private double price;
    private String buyerName;
    private String buyerPhone;
    private String sellerName;
    private String sellerPhone;
    private String location;
    private Date scheduledTime;
    private String status;
}