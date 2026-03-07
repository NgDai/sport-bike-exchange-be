package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.config.VNPayConfig;
import com.bicycle.marketplace.dto.request.VNPayRequest;
import com.bicycle.marketplace.dto.response.VNPayResponse;
import com.bicycle.marketplace.services.VNPayService;
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

    @PostMapping("/submitOrder")
    public VNPayResponse submitOrder(@RequestBody VNPayRequest request, HttpServletRequest httpRequest){
        String baseUrl = httpRequest.getScheme() + "://" + httpRequest.getServerName() + ":" + httpRequest.getServerPort();
        String vnpayUrl = vnPayService.createOrder(request.getAmount(), request.getOrderInfo(), baseUrl);
        VNPayResponse response = new VNPayResponse();
        response.setRedirectUrl(vnpayUrl);
        return response;
    }

    @GetMapping("/vnpay-payment")
    public VNPayResponse handleVnPayReturn(HttpServletRequest request){
        int paymentStatus = vnPayService.orderReturn(request);

        VNPayResponse response = new VNPayResponse();
        response.setPaymentStatus(paymentStatus);
        response.setOrderInfo(request.getParameter("vnp_OrderInfo"));
        response.setPaymentTime(request.getParameter("vnp_PayDate"));
        response.setTransactionId(request.getParameter("vnp_TransactionNo"));
        response.setTotalPrice(request.getParameter("vnp_Amount"));

        return response;
    }
}
