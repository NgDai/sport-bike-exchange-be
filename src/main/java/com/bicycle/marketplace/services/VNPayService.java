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

    private String buildQueryWithSecureHash(
            Map<String, String> params,
            String encodingCharset
    ) {
        List<String> sortedKeys = new ArrayList<>(params.keySet());
        Collections.sort(sortedKeys);

        StringBuilder hashInput = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < sortedKeys.size(); i++) {
            String key = sortedKeys.get(i);
            String value = params.get(key);
            if (value == null) {
                value = "";
            }
            if (hashInput.length() > 0) {
                hashInput.append('&');
                query.append('&');
            }
            try {
                String encKey = URLEncoder.encode(key, encodingCharset);
                String encValue = URLEncoder.encode(value, encodingCharset);
                hashInput.append(encKey).append('=').append(encValue);
                query.append(encKey).append('=').append(encValue);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("VNPay encoding error", e);
            }
        }
        String secureHash = VNPayConfig.hmacSHA512(
                VNPayConfig.secretKey,
                hashInput.toString()
        );
        return query + "&vnp_SecureHash=" + secureHash;
    }

    private Map<String, String> collectReturnParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = request.getParameter(name);
            params.put(name, value != null ? value : "");
        }
        return params;
    }

    private boolean isSignatureValid(
            Map<String, String> params,
            String receivedHash,
            String rawQueryString
    ) {
        if (receivedHash == null) {
            return false;
        }
    }
}
