package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.config.VNPayConfig;
import com.bicycle.marketplace.dto.request.VNPayRequest;
import com.bicycle.marketplace.dto.response.VNPayResponse;
import com.bicycle.marketplace.services.DepositService;
import com.bicycle.marketplace.services.EventBicycleService;
import com.bicycle.marketplace.services.PostingService;
import com.bicycle.marketplace.services.ReservationService;
import com.bicycle.marketplace.services.VNPayService;
import com.bicycle.marketplace.services.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payments")
@Slf4j
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private DepositService depositService;

    @Autowired
    private PostingService postingService;

    @Autowired
    private EventBicycleService eventBicycleService;

    @Autowired
    private ReservationService reservationService;

    // Đọc Base URL của Frontend từ file properties (Hỗ trợ Local & Vercel)
    @Value("${frontend.url:http://localhost:5173}")
    private String frontendBaseUrl;

    // 1. API GỌI TỪ FRONTEND ĐỂ NẠP TIỀN VÀO VÍ (NẠP THƯỜNG)
    @PostMapping("/submitOrder")
    public VNPayResponse submitOrder(
            @RequestBody VNPayRequest request,
            HttpServletRequest httpRequest) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        String secureOrderInfo = username + "|topup|" + request.getOrderInfo();

        String clientIp = VNPayConfig.getIpAddress(httpRequest);
        String vnpayUrl = vnPayService.createOrder(
                request.getAmount(),
                secureOrderInfo,
                request.getReturnUrl(),
                clientIp);

        VNPayResponse response = new VNPayResponse();
        response.setRedirectUrl(vnpayUrl);
        return response;
    }

    // 2. WEBHOOK/IPN (VNPAY GỌI NGẦM XUỐNG ĐỂ XÁC NHẬN GIAO DỊCH - Dùng cho BE)
    @GetMapping("/vnpay-wallet")
    public VNPayResponse handleVnPayWallet(HttpServletRequest request) {
        int paymentStatus = vnPayService.orderReturn(request);

        VNPayResponse response = new VNPayResponse();
        response.setPaymentStatus(paymentStatus);
        response.setOrderInfo(request.getParameter("vnp_OrderInfo"));
        response.setPaymentTime(request.getParameter("vnp_PayDate"));
        response.setTransactionId(request.getParameter("vnp_TransactionNo"));
        response.setTotalPrice(request.getParameter("vnp_Amount"));

        String orderInfo = request.getParameter("vnp_OrderInfo");
        if (orderInfo == null || !orderInfo.contains("|")) {
            throw new RuntimeException("Thông tin đơn hàng không hợp lệ.");
        }

        String username = parseUsername(orderInfo);
        int targetId = parseId(orderInfo);

        if (paymentStatus == 1) {
            double amount = Double.parseDouble(request.getParameter("vnp_Amount")) / 100;

            if (isDepositPayment(orderInfo)) {
                depositService.confirmDepositPayment(targetId, username, amount);
                response.setMessage("Đặt cọc xe thành công.");
            } else if (isEventDepositPayment(orderInfo)) {
                depositService.confirmDepositPaymentForEvent(targetId, username, amount);
                response.setMessage("Đặt cọc xe sự kiện thành công.");
            } else if (isPostFeePayment(orderInfo)) {
                postingService.confirmPostingPayment(targetId, username, amount);
                response.setMessage("Thanh toán phí đăng bài thành công.");
            } else if (isEventFeePayment(orderInfo)) {
                eventBicycleService.confirmEventBicyclePayment(targetId, username, amount);
                response.setMessage("Thanh toán phí đăng ký event thành công.");
            } else if (isFinalPayment(orderInfo)) {
                reservationService.confirmFinalPayment(targetId, username, amount);
                response.setMessage("Thanh toán cuối giao dịch thành công.");
            } else if (isFinalPaymentEventBicycle(orderInfo)) {
                reservationService.confirmFinalPaymentForEventBicycle(targetId, username, amount);
                response.setMessage("Thanh toán giao dịch xe sự kiện thành công");
            } else if (isTopUpPayment(orderInfo)) {
                walletService.addFundsToUserWallet(amount, username);
                response.setMessage("Nạp tiền vào ví thành công.");
            }
        } else {
            if (isDepositPayment(orderInfo) || isEventDepositPayment(orderInfo)) {
                try { depositService.cancelDepositPayment(targetId); } catch (Exception e) { log.warn("Lỗi khi hủy cọc: {}", e.getMessage()); }
            } else if (isPostFeePayment(orderInfo)) {
                try { postingService.cancelPostingPayment(targetId); } catch (Exception e) { log.warn("Lỗi khi hủy đăng bài: {}", e.getMessage()); }
            } else if (isEventFeePayment(orderInfo)) {
                try { eventBicycleService.cancelEventBicyclePayment(targetId); } catch (Exception e) { log.warn("Lỗi khi hủy event bicycle: {}", e.getMessage()); }
            } else if (isFinalPayment(orderInfo) || isFinalPaymentEventBicycle(orderInfo)) {
                try { reservationService.cancelFinalPayment(targetId); } catch (Exception e) { log.warn("Lỗi khi hủy thanh toán cuối: {}", e.getMessage()); }
            }
            throw new RuntimeException("Giao dịch thanh toán không thành công hoặc đã bị hủy.");
        }
        return response;
    }

    // 3. RETURN URL (SAU KHI USER THANH TOÁN XONG SẼ BỊ ĐIỀU HƯỚNG VỀ ĐÂY)
    @GetMapping("/vnpay-payment")
    public void handleVnPayReturn(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int paymentStatus = vnPayService.orderReturn(request);
        String orderInfo = request.getParameter("vnp_OrderInfo");

        if (orderInfo == null || !orderInfo.contains("|")) {
            response.sendRedirect(frontendBaseUrl);
            return;
        }

        String username = parseUsername(orderInfo);
        int targetId = parseId(orderInfo);

        // =========================================================
        // TRƯỜNG HỢP 1: THANH TOÁN THÀNH CÔNG
        // =========================================================
        if (paymentStatus == 1) {
            String vnpAmountStr = request.getParameter("vnp_Amount");
            double amount = (vnpAmountStr != null && !vnpAmountStr.isEmpty()) ? Double.parseDouble(vnpAmountStr) / 100 : 0;

            if (isDepositPayment(orderInfo)) {
                depositService.confirmDepositPayment(targetId, username, amount);
                response.sendRedirect(frontendBaseUrl + "/profile?tab=transaction-manage");
                return;

            } else if (isEventDepositPayment(orderInfo)) {
                // ĐẶT CỌC XE SỰ KIỆN THÀNH CÔNG
                depositService.confirmDepositPaymentForEvent(targetId, username, amount);
                response.sendRedirect(frontendBaseUrl + "/profile?tab=transaction-manage");
                return;

            } else if (isPostFeePayment(orderInfo)) {
                postingService.confirmPostingPayment(targetId, username, amount);
                response.sendRedirect(frontendBaseUrl + "/profile?tab=my-bikes");
                return;

            } else if (isEventFeePayment(orderInfo)) {
                int eventId = eventBicycleService.getEventIdByEventBikeId(targetId);
                eventBicycleService.confirmEventBicyclePayment(targetId, username, amount);
                response.sendRedirect(frontendBaseUrl + "/events/" + eventId);
                return;

            } else if (isFinalPayment(orderInfo)) {
                reservationService.confirmFinalPayment(targetId, username, amount);
                response.sendRedirect(frontendBaseUrl + "/profile?tab=transaction-manage");
                return;
            } else if (isFinalPaymentEventBicycle(orderInfo)) {
                reservationService.confirmFinalPaymentForEventBicycle(targetId, username, amount);
                response.sendRedirect(frontendBaseUrl + "/profile?tab=transaction-manage");
                return;
            } else if (isTopUpPayment(orderInfo)) {
                walletService.addFundsToUserWallet(amount, username);
                response.sendRedirect(frontendBaseUrl + "/profile?tab=wallet");
                return;
            }
        }

        // =========================================================
        // TRƯỜNG HỢP 2: THANH TOÁN THẤT BẠI / NGƯỜI DÙNG BẤM HỦY
        // =========================================================
        else {
            if (isDepositPayment(orderInfo)) {
                Integer listingId = depositService.getListingIdByDepositId(targetId);
                try {
                    depositService.cancelDepositPayment(targetId);
                } catch (Exception e) {
                    log.warn("Lỗi khi hủy cọc: {}", e.getMessage());
                }
                if (listingId != null) {
                    response.sendRedirect(frontendBaseUrl + "/bikes/" + listingId);
                } else {
                    response.sendRedirect(frontendBaseUrl + "/bikes");
                }
                return;

            } else if (isEventDepositPayment(orderInfo)) {
                // HỦY ĐẶT CỌC XE SỰ KIỆN
                Integer eventId = depositService.getEventIdByDepositId(targetId);
                try {
                    depositService.cancelDepositPayment(targetId);
                } catch (Exception e) {
                    log.warn("Lỗi khi hủy cọc sự kiện: {}", e.getMessage());
                }

                if (eventId != null) {
                    response.sendRedirect(frontendBaseUrl + "/events/" + eventId);
                } else {
                    response.sendRedirect(frontendBaseUrl + "/events");
                }
                return;

            } else if (isPostFeePayment(orderInfo)) {
                try {
                    postingService.cancelPostingPayment(targetId);
                } catch (Exception e) {
                    log.warn("Lỗi khi hủy đăng bài: {}", e.getMessage());
                }
                response.sendRedirect(frontendBaseUrl + "/post-bike");
                return;

            } else if (isEventFeePayment(orderInfo)) {
                int eventId = 0;
                try {
                    eventId = eventBicycleService.getEventIdByEventBikeId(targetId);
                    eventBicycleService.cancelEventBicyclePayment(targetId);
                } catch (Exception e) {
                    log.warn("Lỗi khi lấy thông tin sự kiện để hủy: {}", e.getMessage());
                }
                if (eventId > 0) {
                    response.sendRedirect(frontendBaseUrl + "/events/" + eventId);
                } else {
                    response.sendRedirect(frontendBaseUrl + "/events");
                }
                return;

            } else if (isFinalPayment(orderInfo)) {
                // Người dùng hủy hoặc thoát VNPay — giữ nguyên Waiting_Payment để thử lại
                try { reservationService.cancelFinalPayment(targetId); } catch (Exception e) { log.warn("Lỗi khi xử lý hủy thanh toán cuối: {}", e.getMessage()); }
                response.sendRedirect(frontendBaseUrl + "/profile?tab=transaction-manage");
                return;
            } else {
                response.sendRedirect(frontendBaseUrl + "/profile?tab=wallet");
                return;
            }
        }

        response.sendRedirect(frontendBaseUrl);
    }

    // ==========================================
    // CÁC HÀM HỖ TRỢ XỬ LÝ CHUỖI ORDER_INFO
    // ==========================================

    private String parseUsername(String orderInfo) {
        return orderInfo.split("\\|")[0].trim();
    }

    private boolean isPostFeePayment(String orderInfo) {
        String[] parts = orderInfo.split("\\|");
        return parts.length >= 3 && "postfee".equalsIgnoreCase(parts[1].trim());
    }

    private boolean isDepositPayment(String orderInfo) {
        String[] parts = orderInfo.split("\\|");
        return parts.length >= 3 && "deposit".equalsIgnoreCase(parts[1].trim());
    }

    // --- THÊM HÀM KIỂM TRA TAG eventdeposit ---
    private boolean isEventDepositPayment(String orderInfo) {
        String[] parts = orderInfo.split("\\|");
        return parts.length >= 3 && "eventdeposit".equalsIgnoreCase(parts[1].trim());
    }

    private boolean isEventFeePayment(String orderInfo) {
        String[] parts = orderInfo.split("\\|");
        return parts.length >= 3 && "eventfee".equalsIgnoreCase(parts[1].trim());
    }

    private boolean isFinalPayment(String orderInfo) {
        String[] parts = orderInfo.split("\\|");
        return parts.length >= 3 && "finalpayment".equalsIgnoreCase(parts[1].trim());
    }

    private boolean isFinalPaymentEventBicycle(String orderInfo) {
        String[] parts = orderInfo.split("\\|");
        return parts.length >= 3 && "finalpaymentEventbicycle".equalsIgnoreCase(parts[1].trim());
    }

    private boolean isTopUpPayment(String orderInfo) {
        String[] parts = orderInfo.split("\\|");
        return parts.length >= 3 && "topup".equalsIgnoreCase(parts[1].trim());
    }

    // Sửa lại hàm parseId ở cuối file PaymentController.java
    private int parseId(String orderInfo) {
        try {
            String[] parts = orderInfo.split("\\|");
            if (parts.length >= 3) {
                return Integer.parseInt(parts[2].trim());
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return 0;
    }
}