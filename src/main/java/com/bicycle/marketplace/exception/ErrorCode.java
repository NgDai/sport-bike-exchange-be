package com.bicycle.marketplace.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USERNAME_ALREADY_EXISTS(1001, "Tên đăng nhập đã tồn tại"),
    USER_NOT_FOUND(1002, "Không tìm thấy người dùng"),
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi chưa phân loại"),
    PASSWORD_INVALID(1003, "Mật khẩu không hợp lệ"),
    INVALID_KEY(1004, "Khóa thông báo không hợp lệ"),
    USERNAME_REQUIRED(1010, "Tên đăng nhập là bắt buộc"),
    PASSWORD_REQUIRED(1011, "Mật khẩu là bắt buộc"),
    USERNAME_INVALID_LENGTH(1012, "Tên đăng nhập phải từ 3 đến 50 ký tự"),
    FULL_NAME_REQUIRED(1013, "Họ tên là bắt buộc"),
    FULL_NAME_MAX_LENGTH(1014, "Họ tên không được vượt quá 100 ký tự"),
    FULL_NAME_NO_NUMBERS(1027, "Họ tên không được chứa chữ số"),
    EMAIL_REQUIRED(1015, "Email là bắt buộc"),
    EMAIL_INVALID(1016, "Định dạng email không hợp lệ"),
    PHONE_MAX_LENGTH(1017, "Số điện thoại không được vượt quá 20 ký tự"),
    USERNAME_INVALID_FORMAT(1022, "Tên đăng nhập phải có cả chữ và số (chỉ chữ, số, gạch dưới hoặc gạch ngang)"),
    PASSWORD_INVALID_FORMAT(1023, "Mật khẩu phải có ít nhất một chữ và một số"),
    PHONE_INVALID_FORMAT(1024, "Định dạng số điện thoại không hợp lệ"),
    PHONE_REQUIRED(1026, "Số điện thoại là bắt buộc"),
    EMAIL_ALREADY_EXISTS(1025, "Email đã tồn tại"),
    STATUS_MAX_LENGTH(1018, "Trạng thái không được vượt quá 50 ký tự"),
    LISTING_NOT_FOUND(1005, "Không tìm thấy bài đăng/niêm yết"),
    USER_INVALID_AUTHENTICATION(1006, "Xác thực người dùng thất bại"),
    EVENT_NOT_FOUND(1007, "Không tìm thấy sự kiện"),
    POSTING_SELLER_ID_REQUIRED(1008, "Mã người bán là bắt buộc"),
    POSTING_EVENT_ID_REQUIRED(1009, "Mã sự kiện là bắt buộc"),
    CHECKIN_NOT_FOUND(1010, "Không tìm thấy bản ghi check-in"),
    DEPOSIT_NOT_FOUND(1011, "Không tìm thấy bản ghi đặt cọc"),
    BICYCLE_NOT_FOUND(1012, "Không tìm thấy xe đạp"),
    DISPUTE_NOT_FOUND(1013, "Không tìm thấy tranh chấp"),
    INSPECTIONREPORT_NOT_FOUND(1014, "Không tìm thấy báo cáo kiểm định"),
    RESERVATION_NOT_FOUND(1015, "Không tìm thấy đặt chỗ"),
    TRANSACTION_NOT_FOUND(1016, "Không tìm thấy giao dịch"),
    BRAND_NOT_FOUND(1017, "Không tìm thấy thương hiệu"),
    CATEGORY_NOT_FOUND(1018, "Không tìm thấy danh mục"),
    EVENT_BICYCLE_NOT_FOUND(1019, "Không tìm thấy xe đạp trong sự kiện"),
    WISHLIST_NOT_FOUND(1020, "Không tìm thấy danh sách yêu thích"),
    LISTING_NOT_PENDING(1021, "Chỉ bài đăng đang chờ mới có thể thêm hoặc cập nhật thông tin xe đạp")
    ;

    private int code;
    private String message;
}
