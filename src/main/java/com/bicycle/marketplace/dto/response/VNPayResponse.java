package com.bicycle.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class VNPayResponse {

    private String redirectUrl;

    private Integer paymentStatus;

    private String orderInfo;

    private String paymentTime;

    private String transactionId;

    private String totalPrice;

    /** Thông báo khi xử lý return (VD: "Thanh toán thành công", "Chữ ký không hợp lệ"). */
    private String message;
}

