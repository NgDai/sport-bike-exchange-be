package com.bicycle.marketplace.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreationRequest {
    private Integer eventId;
    private Integer listingId;
    private Integer buyerId;
    private Integer sellerId;

    @NotNull(message = "Mã đặt cọc (depositId) là bắt buộc khi tạo giao dịch")
    private Integer depositId;

    @NotNull(message = "Mã đặt chỗ (reservationId) là bắt buộc khi tạo giao dịch")
    private Integer reservationId;

    private String status;
    private double amount;
    private double actualPrice;
}
