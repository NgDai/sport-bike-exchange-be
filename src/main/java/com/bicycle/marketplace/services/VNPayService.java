package com.bicycle.marketplace.services;

import com.bicycle.marketplace.config.VNPayConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VNPayService {

    private static final String VNP_TRANSACTION_STATUS_SUCCESS = "00";
    private static final String VNP_SECURE_HASH_PARAM = "vnp_SecureHash";
    private static final String VNP_SECURE_HASH_TYPE_PARAM = "vnp_SecureHashType";
    private static final String DEFAULT_CLIENT_IP = "127.0.0.1";
    private static final String CHARSET_ASCII = StandardCharsets.US_ASCII.name();

    /**
     * Builds the full VNPay payment URL (version 2.1.0).
     * Hash input uses URL-encoded key=value per VNPay 2.1.0.
     */
    public String createOrder(
            int amountVnd,
            String orderInfo,
            String baseUrl,
            String clientIp
    ) {
        Map<String, String> params =
                buildPaymentParams(amountVnd, orderInfo, baseUrl, clientIp);
        String queryWithHash = buildQueryWithSecureHash(params, CHARSET_ASCII);
        return VNPayConfig.vnp_PayUrl + "?" + queryWithHash;
    }

    /**
     * Handles VNPay redirect to Return URL: validates signature and returns result code.
     *
     * @return 1 = success, 0 = failed/cancelled, -1 = invalid signature
     */
    public int orderReturn(HttpServletRequest request) {
        Map<String, String> params = collectReturnParams(request);
        String receivedHash = request.getParameter(VNP_SECURE_HASH_PARAM);
        params.remove(VNP_SECURE_HASH_PARAM);
        params.remove(VNP_SECURE_HASH_TYPE_PARAM);

        if (!isSignatureValid(params, receivedHash, request.getQueryString())) {
            log.warn("VNPay return: invalid signature");
            return -1;
        }
        String txnStatus = request.getParameter("vnp_TransactionStatus");
        return VNP_TRANSACTION_STATUS_SUCCESS.equals(txnStatus) ? 1 : 0;
    }

    private Map<String, String> buildPaymentParams(
            int amountVnd,
            String orderInfo,
            String baseUrl,
            String clientIp
    ) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(amountVnd * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", VNPayConfig.getRandomNumber(8));
        params.put("vnp_OrderInfo", orderInfo != null ? orderInfo : "");
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", buildReturnUrl(baseUrl));
        String ip = (clientIp != null && !clientIp.isEmpty())
                ? clientIp
                : DEFAULT_CLIENT_IP;
        params.put("vnp_IpAddr", ip);
        params.put("vnp_CreateDate", VNPayConfig.formatCreateDate());
        params.put("vnp_ExpireDate", VNPayConfig.formatExpireDate());
        return params;
    }

    private String buildReturnUrl(String baseUrl) {
        return (baseUrl != null ? baseUrl.replaceAll("/+$", "") : "") + VNPayConfig.vnp_ReturnUrl;
    }

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }

    public int orderReturn(HttpServletRequest request){
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = VNPayConfig.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }
}
