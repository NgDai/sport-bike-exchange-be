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
public class CreatePostingResponse {

    /** Bài đăng vừa tạo (null nếu ví không đủ tiền) */
    PostingResponse listing;

    /** URL thanh toán VNPay (null nếu đã trừ ví thành công) */
    String paymentUrl;

    /** Mô tả kết quả */
    String message;
}
