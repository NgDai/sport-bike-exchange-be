package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.config.VNPayConfig;
import com.bicycle.marketplace.dto.request.VNPayRequest;
import com.bicycle.marketplace.dto.response.VNPayResponse;
import com.bicycle.marketplace.services.DepositService;
import com.bicycle.marketplace.services.PostingService;
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

    /** URL frontend sau khi ĐĂNG BÀI thành công */
    @Value("${vnpay.frontend.post-success-url:http://localhost:5173/profile?tab=my-bikes}")
    private String postSuccessUrl;

    /** URL frontend sau khi NẠP VÍ thành công hoặc LỖI chung */
    @Value("${vnpay.frontend.wallet-url:http://localhost:5173/profile?tab=wallet}")
    private String walletSuccessUrl;

    /** URL frontend sau khi ĐẶT CỌC thành công */
    private final String depositSuccessUrl = "http://localhost:5173/profile?tab=transaction-manage";

    // 1. API GỌI TỪ FRONTEND ĐỂ NẠP TIỀN VÀO VÍ (NẠP THƯỜNG)
    @PostMapping("/submitOrder")
    public VNPayResponse submitOrder(
            @RequestBody VNPayRequest request,
            HttpServletRequest httpRequest) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Gắn tag "topup" để phân biệt với cọc (deposit) và phí (fee)
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

    // 2. WEBHOOK/IPN (VNPAY GỌI NGẦM XUỐNG ĐỂ XÁC NHẬN GIAO DỊCH)
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
        String username = parseUsername(orderInfo);

        if (paymentStatus == 1) {
            double amount = Double.parseDouble(request.getParameter("vnp_Amount")) / 100;

            if (isDepositPayment(orderInfo)) {
                int depositId = parseId(orderInfo);
                depositService.confirmDepositPayment(depositId, username, amount);
                response.setMessage("Đặt cọc xe thành công.");

            } else if (isFeePayment(orderInfo)) {
                int listingId = parseId(orderInfo);
                postingService.confirmPaymentAndPublish(listingId, username, amount);
                response.setMessage("Thanh toán phí đăng bài thành công.");

            } else {
                walletService.addFundsToUserWallet(amount, username);
                response.setMessage("Nạp tiền vào ví thành công.");
            }
        } else {
            // Giao dịch lỗi / Hủy
            if (isDepositPayment(orderInfo)) {
                try {
                    depositService.cancelDepositPayment(parseId(orderInfo));
                } catch (Exception e) {
                    log.warn("Lỗi khi hủy cọc: {}", e.getMessage());
                }
            } else if (isFeePayment(orderInfo)) {
                try {
                    postingService.cancelPostingPayment(parseId(orderInfo));
                } catch (Exception e) {
                    log.warn("Lỗi khi hủy đăng bài: {}", e.getMessage());
                }
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
        String username = parseUsername(orderInfo);

        if (paymentStatus == 1) {
            double amount = Double.parseDouble(request.getParameter("vnp_Amount")) / 100;

            if (isDepositPayment(orderInfo)) {
                int depositId = parseId(orderInfo);
                depositService.confirmDepositPayment(depositId, username, amount);
                response.sendRedirect(depositSuccessUrl);

            } else if (isFeePayment(orderInfo)) {
                int listingId = parseId(orderInfo);
                postingService.confirmPaymentAndPublish(listingId, username, amount);
                response.sendRedirect(postSuccessUrl);

            } else {
                walletService.addFundsToUserWallet(amount, username);
                response.sendRedirect(walletSuccessUrl);
            }
        } else {
            // Thanh toán lỗi hoặc người dùng bấm Hủy
            if (isDepositPayment(orderInfo)) {
                try {
                    depositService.cancelDepositPayment(parseId(orderInfo));
                } catch (Exception e) {
                    log.warn("Lỗi khi hủy cọc: {}", e.getMessage());
                }
            } else if (isFeePayment(orderInfo)) {
                try {
                    postingService.cancelPostingPayment(parseId(orderInfo));
                } catch (Exception e) {
                    log.warn("Lỗi khi hủy đăng bài: {}", e.getMessage());
                }
            }
            // Điều hướng về trang ví hoặc hiển thị lỗi
            response.sendRedirect(walletSuccessUrl + "?status=failed");
        }
    }

    // ==========================================
    // CÁC HÀM HỖ TRỢ XỬ LÝ CHUỖI ORDER_INFO
    // ==========================================

    /** Format mẫu: "username|type|id" */
    private String parseUsername(String orderInfo) {
        if (orderInfo == null || !orderInfo.contains("|")) return "";
        return orderInfo.split("\\|")[0].trim();
    }

    private boolean isFeePayment(String orderInfo) {
        if (orderInfo == null) return false;
        String[] parts = orderInfo.split("\\|");
        return parts.length >= 3 && "fee".equalsIgnoreCase(parts[1].trim());
    }

    private boolean isDepositPayment(String orderInfo) {
        if (orderInfo == null) return false;
        String[] parts = orderInfo.split("\\|");
        return parts.length >= 3 && "deposit".equalsIgnoreCase(parts[1].trim());
    }

    /** Lấy ra ID (listingId hoặc depositId) từ chuỗi */
    private int parseId(String orderInfo) {
        String[] parts = orderInfo.split("\\|");
        return Integer.parseInt(parts[2].trim());
    }
}