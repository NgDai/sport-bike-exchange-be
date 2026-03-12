package com.bicycle.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DepositCreationRequest {
    private String type;
    private double amount;
    private String status;
    private Date createAt;
    /** ID bài đăng xe – bắt buộc khi đặt cọc để tạo giao dịch */
    private Integer listingId;
    /** ID đặt chỗ – khi có thì sau khi tạo deposit sẽ tự tạo Transaction (luồng đặt cọc) */
    private Integer reservationId;
    /** Giá thực tế giao dịch – tùy chọn; không gửi thì lấy từ giá listing */
    private Double actualPrice;
}
