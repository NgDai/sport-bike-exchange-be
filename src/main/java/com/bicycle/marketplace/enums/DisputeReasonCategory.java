package com.bicycle.marketplace.enums;

/**
 * Phân loại lý do tranh chấp.
 */
public enum DisputeReasonCategory {
    MISDESCRIPTION,      // Mô tả không đúng / seller không trung thực
    NON_DELIVERY,        // Không giao xe / mất liên lạc
    BUYER_DEFAULT,       // Buyer không nhận xe / không thanh toán đủ
    DAMAGE_ON_DELIVERY,  // Hư hỏng khi giao
    PAYMENT_ISSUE,       // Tranh chấp tiền / chưa về / sai số tiền
    CANCELLATION,        // Đổi ý / hủy đơn không thỏa thuận
    OTHER                // Khác
}
