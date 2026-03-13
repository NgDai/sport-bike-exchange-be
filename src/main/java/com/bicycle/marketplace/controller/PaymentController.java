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
    private com.bicycle.marketplace.services.DepositService depositService;

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
            if (isDepositPayment(orderInfo)) {
                // Thanh toán đặt cọc thành công → confirm deposit
                int depositId = parseDepositId(orderInfo);
                depositService.confirmDepositPayment(depositId, username, amount);
                response.setMessage("Đặt cọc thành công qua VNPay");
            } else if (isFeePayment(orderInfo)) {
                walletService.addFundsToSystemWallet(amount, username);
                response.setMessage("Thanh toán phí đăng bài thành công");
            } else {
                walletService.addFundsToUserWallet(amount, username);
                response.setMessage("Nạp ví thành công");
            }
        } else {
            // Thanh toán thất bại hoặc hủy
            String orderInfo = request.getParameter("vnp_OrderInfo");
            if (isDepositPayment(orderInfo)) {
                int depositId = parseDepositId(orderInfo);
                try {
                    depositService.cancelDepositPayment(depositId);
                } catch (Exception e) {
                    log.warn("Cancel deposit payment failed for depositId={}: {}", depositId, e.getMessage());
                }
            }
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
            if (isDepositPayment(orderInfo)) {
                int depositId = parseDepositId(orderInfo);
                depositService.confirmDepositPayment(depositId, username, amount);
                response.sendRedirect(postSuccessUrl);
            } else if (isFeePayment(orderInfo)) {
                walletService.addFundsToSystemWallet(amount, username);
                response.sendRedirect(postSuccessUrl);
            } else {
                walletService.addFundsToUserWallet(amount, username);
                response.sendRedirect(walletSuccessUrl);
            }
        } else {
            // Thanh toán thất bại hoặc hủy → nếu là deposit thì hủy và trả listing về Available
            if (isDepositPayment(orderInfo)) {
                int depositId = parseDepositId(orderInfo);
                try {
                    depositService.cancelDepositPayment(depositId);
                } catch (Exception e) {
                    log.warn("Cancel deposit payment failed for depositId={}: {}", depositId, e.getMessage());
                }
            }
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
    /** Format đặt cọc: "username|deposit|depositId" — parts[1] == "deposit" */
    private boolean isDepositPayment(String orderInfo) {
        if (orderInfo == null) return false;
        String[] parts = orderInfo.split("\\|");
        return parts.length >= 2 && "deposit".equalsIgnoreCase(parts[1].trim());
    }

    /** Parse depositId từ orderInfo dạng "username|deposit|depositId" */
    private int parseDepositId(String orderInfo) {
        String[] parts = orderInfo.split("\\|");
        return Integer.parseInt(parts[2].trim());
    }

}
