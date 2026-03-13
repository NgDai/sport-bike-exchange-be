package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private int reservationId;

    // Thông tin cơ bản
    private int listingId;
    private String listingTitle;
    private int buyerId;
    private String buyerName;

    // --- BỔ SUNG CÁC TRƯỜNG LỊCH HẸN ---
    private Integer inspectorId;
    private String inspectorName;
    private String inspectorPhone;
    private String meetingLocation;
    private Date meetingTime;
    // -----------------------------------

    private String status;
    private int depositId;
    private Double depositAmount;
    private Date reservedAt;
}