package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.config.VNPayConfig;
import com.bicycle.marketplace.dto.request.VNPayRequest;
import com.bicycle.marketplace.dto.request.WalletAddBalanceRequest;
import com.bicycle.marketplace.dto.response.VNPayResponse;
import com.bicycle.marketplace.services.VNPayService;
import com.bicycle.marketplace.services.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/submitOrder")
    public VNPayResponse submitOrder(
            @RequestBody VNPayRequest request,
            HttpServletRequest httpRequest
    ) {
        String baseUrl = httpRequest.getScheme()
                + "://"
                + httpRequest.getServerName()
                + ":"
                + httpRequest.getServerPort();
        String clientIp = VNPayConfig.getIpAddress(httpRequest);
        String vnpayUrl = vnPayService.createOrder(
                request.getAmount(),
                request.getOrderInfo(),
                request.getReturnUrl(), // Lấy URL do React gửi lên
//                baseUrl,
                clientIp
        );
        VNPayResponse response = new VNPayResponse();
        response.setRedirectUrl(vnpayUrl);
        return response;
    }

//    @GetMapping("/vnpay-payment")
//    public VNPayResponse handleVnPayReturn(HttpServletRequest request) {
//        int paymentStatus = vnPayService.orderReturn(request);
//
//        VNPayResponse response = new VNPayResponse();
//        response.setPaymentStatus(paymentStatus);
//        response.setOrderInfo(request.getParameter("vnp_OrderInfo"));
//        response.setPaymentTime(request.getParameter("vnp_PayDate"));
//        response.setTransactionId(request.getParameter("vnp_TransactionNo"));
//        response.setTotalPrice(request.getParameter("vnp_Amount"));
//
////        if (paymentStatus == 1) {
////            response.setMessage("Thanh toán thành công");
////        } else if (paymentStatus == 0) {
////            response.setMessage("Giao dịch không thành công hoặc đã hủy");
////        } else {
////            throw new RuntimeException("Giao dịch không thành công hoặc chữ ký không hợp lệ");        }
//        if (paymentStatus == 1) {
//            // 2. NẾU THÀNH CÔNG -> GỌI HÀM CỘNG TIỀN VÀO VÍ
//            // Lưu ý: VNPay trả về số tiền nhân với 100, nên ta phải chia lại cho 100
//            double amount = Double.parseDouble(request.getParameter("vnp_Amount")) / 100;
//            walletService.addFunds(new WalletAddBalanceRequest(amount));
//
//            response.setMessage("Thanh toán thành công");
//        } else {
//            // Ném lỗi để Frontend bắt được (Tránh cộng tiền sai)
//            throw new RuntimeException("Giao dịch không thành công hoặc chữ ký không hợp lệ");
//        }
//        return response;
//    }

@GetMapping("/vnpay-payment")
public VNPayResponse handleVnPayReturn(HttpServletRequest request) {
    int paymentStatus = vnPayService.orderReturn(request);

    VNPayResponse response = new VNPayResponse();
    response.setPaymentStatus(paymentStatus);
    response.setOrderInfo(request.getParameter("vnp_OrderInfo"));
    response.setPaymentTime(request.getParameter("vnp_PayDate"));
    response.setTransactionId(request.getParameter("vnp_TransactionNo"));
    response.setTotalPrice(request.getParameter("vnp_Amount"));


    if (paymentStatus == 1) {
        // LẤY MÃ GIAO DỊCH VNPay
        String txnRef = request.getParameter("vnp_TxnRef");

        // TẠI ĐÂY BẠN NÊN CÓ LOGIC CHECK DATABASE
        // Ví dụ: boolean isProcessed = walletTransactionService.checkIfExists(txnRef);
        // if (isProcessed) {
        //      throw new RuntimeException("Giao dịch này đã được xử lý rồi!");
        // }

        double amount = Double.parseDouble(request.getParameter("vnp_Amount")) / 100;
        walletService.addFunds(new WalletAddBalanceRequest(amount));

        // SAU KHI CỘNG TIỀN, LƯU txnRef VÀO DATABASE ĐỂ ĐÁNH DẤU LÀ ĐÃ XỬ LÝ

        response.setMessage("Thanh toán thành công");
    } else {
        throw new RuntimeException("Giao dịch không thành công hoặc chữ ký không hợp lệ");
    }
    return response;
}
}
