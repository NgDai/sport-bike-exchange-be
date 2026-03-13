package com.bicycle.marketplace.enums;

/**
 * Trạng thái tranh chấp.
 */
public enum DisputeStatus {
    OPEN,        // Mới tạo, chờ xử lý
    ASSIGNED,    // Đã gán inspector
    INSPECTING,  // Inspector đang kiểm tra
    RESOLVED,    // Đã giải quyết (có kết quả inspection)
    CLOSED       // Đóng (không cần xử lý / hủy)
}
