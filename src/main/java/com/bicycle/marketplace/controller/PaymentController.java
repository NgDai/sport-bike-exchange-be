package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.config.VNPayConfig;
import com.bicycle.marketplace.dto.request.VNPayRequest;
import com.bicycle.marketplace.dto.response.VNPayResponse;
import com.bicycle.marketplace.services.VNPayService;
import com.bicycle.marketplace.services.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private WalletService walletService;

    /** URL frontend sau khi đăng bài thành công */
    @Value("${vnpay.frontend.post-success-url:http://localhost:5173/profile?tab=my-bikes}")
    private String postSuccessUrl;

    /** URL frontend sau khi nạp ví thành công */
    @Value("${vnpay.frontend.wallet-url:http://localhost:5173/profile?tab=wallet}")
    private String walletSuccessUrl;

    @PostMapping("/submitOrder")
    public VNPayResponse submitOrder(
            @RequestBody VNPayRequest request,
            HttpServletRequest httpRequest) {

        // 1. Get username from login session
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();

        // 2. Add username into orderInfo so we can retrieve it on callback
        String secureOrderInfo = username + "|" + request.getOrderInfo();

        String baseUrl = httpRequest.getScheme()
                + "://"
                + httpRequest.getServerName()
                + ":"
                + httpRequest.getServerPort();
        String clientIp = VNPayConfig.getIpAddress(httpRequest);
        String vnpayUrl = vnPayService.createOrder(
                request.getAmount(),
                secureOrderInfo, // Pass the custom order info string
                request.getReturnUrl(), // Lấy URL do React gửi lên
                // baseUrl,
                clientIp);
        VNPayResponse response = new VNPayResponse();
        response.setRedirectUrl(vnpayUrl);
        return response;
    }

    @GetMapping("/vnpay-wallet")
    public VNPayResponse handleVnPayWallet(HttpServletRequest request) {
        int paymentStatus = vnPayService.orderReturn(request);

        VNPayResponse response = new VNPayResponse();
        response.setPaymentStatus(paymentStatus);
        response.setOrderInfo(request.getParameter("vnp_OrderInfo"));
        response.setPaymentTime(request.getParameter("vnp_PayDate"));
        response.setTransactionId(request.getParameter("vnp_TransactionNo"));
        response.setTotalPrice(request.getParameter("vnp_Amount"));

        if (paymentStatus == 1) {
            double amount = Double.parseDouble(request.getParameter("vnp_Amount")) / 100;
            String orderInfo = request.getParameter("vnp_OrderInfo");
            String username = parseUsername(orderInfo);
            // Nếu orderInfo có dạng "username|fee|..." thì đây là phí đăng bài → vào ví System
            if (isFeePayment(orderInfo)) {
                walletService.addFundsToSystemWallet(amount, username);
                response.setMessage("Thanh toán phí đăng bài thành công");
            } else {
                // Nạp ví thông thường → vào ví user (parse username từ orderInfo, không dùng session)
                walletService.addFundsToUserWallet(amount, username);
                response.setMessage("Nạp ví thành công");
            }
        } else {
            throw new RuntimeException("Giao dịch không thành công hoặc chữ ký không hợp lệ");
        }
        return response;
    }

    @GetMapping("/vnpay-payment")
    public void handleVnPayReturn(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int paymentStatus = vnPayService.orderReturn(request);
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String username = parseUsername(orderInfo);

        if (paymentStatus == 1) {
            double amount = Double.parseDouble(request.getParameter("vnp_Amount")) / 100;
            if (isFeePayment(orderInfo)) {
                // Phí đăng bài → tiền vào ví System, redirect về trang đăng bài thành công
                walletService.addFundsToSystemWallet(amount, username);
                response.sendRedirect(postSuccessUrl);
            } else {
                // Nạp ví thường → tiền vào ví user, redirect về trang ví
                walletService.addFundsToUserWallet(amount, username);
                response.sendRedirect(walletSuccessUrl);
            }
        } else {
            response.sendRedirect(walletSuccessUrl + "&status=failed");
        }
    }

    /** Parse phần username (trước dấu |) từ vnp_OrderInfo. */
    private String parseUsername(String orderInfo) {
        if (orderInfo == null || !orderInfo.contains("|")) return "";
        return orderInfo.split("\\|")[0].trim();
    }

    /** Format phí: "username|fee|..." — parts[1] == "fee" */
    private boolean isFeePayment(String orderInfo) {
        if (orderInfo == null) return false;
        String[] parts = orderInfo.split("\\|");
        return parts.length >= 2 && "fee".equalsIgnoreCase(parts[1].trim());
    }

}
