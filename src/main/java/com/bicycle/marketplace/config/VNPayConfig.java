package com.bicycle.marketplace.config;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

public final class VNPayConfig {

    private static final String DATE_FORMAT = "yyyyMMddHHmmss";
    private static final TimeZone VNP_TIMEZONE = TimeZone.getTimeZone("Etc/GMT+7");
    private static final String UTF_8 = StandardCharsets.UTF_8.name();
    private static final String VNP_SECURE_HASH_PREFIX = "vnp_SecureHash=";
    private static final String VNP_SECURE_HASH_TYPE_PREFIX = "vnp_SecureHashType=";

    // --- VNPay endpoint & merchant config ---
    public static final String vnp_PayUrl =
            "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    /**
     * Return path (append to baseUrl).
     * E.g. baseUrl=http://localhost:8080 → full return = baseUrl + vnp_ReturnUrl
     */
    public static final String vnp_ReturnUrl = "/api/payments/vnpay-payment";
    public static final String vnp_TmnCode = "KIY4BYCA";
    public static final String secretKey = "GH8L0AJZ4TBCEQRQ5JAEQVGA5ABH0E44";
    public static final String vnp_ApiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    public static final String vnp_Version = "2.1.0";
    public static final String vnp_Command = "pay";
    public static final String orderType = "other";

    private VNPayConfig() {}

    // --- Date helpers for payment params ---

    public static String formatCreateDate() {
        return formatCalendar(Calendar.getInstance(VNP_TIMEZONE));
    }

    public static String formatExpireDate() {
        Calendar cal = Calendar.getInstance(VNP_TIMEZONE);
        cal.add(Calendar.MINUTE, 15);
        return formatCalendar(cal);
    }

    private static String formatCalendar(Calendar cal) {
        return new java.text.SimpleDateFormat(DATE_FORMAT).format(cal.getTime());
    }

    // --- Hash / HMAC ---

    /**
     * HMAC SHA512; key and data as given; output lowercase hex.
     */
    public static String hmacSHA512(String key, String data) {
        if (key == null || data == null) {
            throw new NullPointerException("key and data must be non-null");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            ));
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(result);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Hash for return verification: sorted key=value (raw, no encoding).
     */
    public static String hashAllFields(Map<String, String> fields) {
        String data = buildSortedKeyValueString(fields);
        return data.isEmpty() ? "" : hmacSHA512(secretKey, data);
    }

    /**
     * Hash for return verification (2.1.0): sorted key=value, URL-encoded (UTF-8).
     */
    public static String hashAllFieldsEncoded(Map<String, String> fields) {
        try {
            String data = buildSortedKeyValueStringEncoded(fields);
            return hmacSHA512(secretKey, data);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * Hash from raw query string after removing vnp_SecureHash and vnp_SecureHashType.
     */
    public static String hashQueryString(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return "";
        }
        String data = queryStringWithoutHashParams(queryString);
        return data.isEmpty() ? "" : hmacSHA512(secretKey, data);
    }

    private static String buildSortedKeyValueString(
            Map<String, String> fields,
            boolean skipEmptyValue
    ) {
        List<String> keys = new ArrayList<>(fields.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = fields.get(key);
            if (skipEmptyValue && (value == null || value.isEmpty())) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key).append("=").append(value != null ? value : "");
        }
        return sb.toString();
    }

    private static String buildSortedKeyValueString(Map<String, String> fields) {
        return buildSortedKeyValueString(fields, true);
    }

    private static String buildSortedKeyValueStringEncoded(Map<String, String> fields)
            throws UnsupportedEncodingException {
        List<String> keys = new ArrayList<>(fields.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = fields.get(key);
            if (value == null) {
                value = "";
            }
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(java.net.URLEncoder.encode(key, UTF_8))
                    .append("=")
                    .append(java.net.URLEncoder.encode(value, UTF_8));
        }
        return sb.toString();
    }

    private static String queryStringWithoutHashParams(String queryString) {
        String[] pairs = queryString.split("&");
        StringBuilder sb = new StringBuilder();
        for (String pair : pairs) {
            if (pair.startsWith(VNP_SECURE_HASH_PREFIX)
                    || pair.startsWith(VNP_SECURE_HASH_TYPE_PREFIX)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(pair);
        }
        return sb.toString();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    // --- Legacy hashes (kept for compatibility; not used by current VNPay 2.1.0 flow) ---

    public static String md5(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return bytesToHex(md.digest(message.getBytes(UTF_8)));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            return "";
        }
    }

    public static String Sha256(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return bytesToHex(md.digest(message.getBytes(UTF_8)));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            return "";
        }
    }

    // --- Request / util ---

    public static String getIpAddress(HttpServletRequest request) {
        try {
            String ip = request.getHeader("X-FORWARDED-FOR");
            return ip != null ? ip : request.getRemoteAddr();
        } catch (Exception e) {
            return "Invalid IP:" + e.getMessage();
        }
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String digits = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(digits.charAt(rnd.nextInt(digits.length())));
        }
        return sb.toString();
    }
}

