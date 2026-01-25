package com.bicycle.marketplace.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT Token Provider using Nimbus JOSE + JWT library.
 * Handles token generation, validation, and extraction of claims.
 */
@Component
public class JWTTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    /**
     * Generate an access token for the given user.
     *
     * @param userDetails the user details
     * @return the generated JWT access token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenExpiration);
    }

    /**
     * Generate a refresh token for the given user.
     *
     * @param userDetails the user details
     * @return the generated JWT refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshTokenExpiration);
    }

    /**
     * Generate a JWT token with specified expiration time.
     *
     * @param userDetails the user details
     * @param expiration  the expiration time in milliseconds
     * @return the generated JWT token
     */
    private String generateToken(UserDetails userDetails, long expiration) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issuer("sport-bike-exchange")
                    .issueTime(new Date())
                    .expirationTime(new Date(
                            Instant.now().plus(expiration, ChronoUnit.MILLIS).toEpochMilli()))
                    .claim("scope", buildScope(userDetails))
                    .build();

            Payload payload = new Payload(claimsSet.toJSONObject());

            JWSObject jwsObject = new JWSObject(header, payload);
            jwsObject.sign(new MACSigner(secretKey.getBytes()));

            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    /**
     * Validate the given token.
     *
     * @param token the JWT token
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes());

            boolean verified = signedJWT.verify(verifier);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            return verified && expirationTime != null && expirationTime.after(new Date());
        } catch (JOSEException | ParseException e) {
            return false;
        }
    }

    /**
     * Extract username from the token.
     *
     * @param token the JWT token
     * @return the username extracted from the token
     */
    public String getUsernameFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }

    /**
     * Extract expiration date from the token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }

    /**
     * Build the scope string from user authorities.
     *
     * @param userDetails the user details
     * @return the scope string
     */
    private String buildScope(UserDetails userDetails) {
        StringBuilder scopeBuilder = new StringBuilder();
        userDetails.getAuthorities().forEach(authority -> {
            if (scopeBuilder.length() > 0) {
                scopeBuilder.append(" ");
            }
            scopeBuilder.append(authority.getAuthority());
        });
        return scopeBuilder.toString();
    }

    /**
     * Get access token expiration time in milliseconds.
     *
     * @return expiration time in milliseconds
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
}
