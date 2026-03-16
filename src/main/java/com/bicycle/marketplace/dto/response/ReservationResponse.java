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

    // Thông tin sản phẩm
    private int listingId;
    private String listingTitle;
    private String listingImage;

    // Thông tin sản phẩm ở sự kiện
    private int eventBikeId;
    private String eventBikeTitle;
    private String eventBikeImage;


    // Người mua
    private int buyerId;
    private String buyerName;

    // Người bán
    private int sellerId;
    private String sellerName;

    // Inspector
    private Integer inspectorId;
    private String inspectorName;
    private String inspectorPhone;

    // Lịch hẹn
    private String meetingLocation;
    private Date meetingTime;
    private Double latitude;
    private Double longitude;

    private String status;
    private int depositId;
    private Double depositAmount;
    private Date reservedAt;
    
    private String cancelDescription;
}