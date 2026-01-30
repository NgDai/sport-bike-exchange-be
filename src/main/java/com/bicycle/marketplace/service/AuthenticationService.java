package com.bicycle.marketplace.service;

import com.bicycle.marketplace.Repository.IUserRepository;
import com.bicycle.marketplace.dto.request.AuthenticationRequest;
import com.bicycle.marketplace.dto.request.IntrospecRequest;
import com.bicycle.marketplace.dto.response.AuthenticationResponse;
import com.bicycle.marketplace.dto.response.IntrospecResponse;
import com.bicycle.marketplace.entity.Users;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Service
@Component
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    IUserRepository userRepository;
    @NonFinal
    //@Value("${jwt.signer.key}")
    protected static String signerKey = "f56614ec2de8b1c09c520066c7f2c595f96271ba791712381cf4fbebdc55759d";

    public IntrospecResponse introspect(IntrospecRequest request) throws ParseException, JOSEException {
        var token = request.getToken();

        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date experationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        return IntrospecResponse.builder()
                .valid(verified && experationTime.after(new Date()))
                .build();

    }

    public AuthenticationResponse authendicate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(5);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) {
            throw new AppException(ErrorCode.USER_INVALID_AUTHENTICATIED);
        }

        var token = generateToken(request.getUsername());

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    private String generateToken(String username) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        Users user = userRepository.findByUsername(username).orElseThrow();
        JWTClaimsSet jwtClaimSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("BicycleMarketplace")
                .claim("userId", user.getUserId())
                .claim("FullName", user.getFullName())
                .claim("Email", user.getEmail())
                .claim("Phone", user.getPhone())
                .claim("WalletBalance", user.getWalletBalance())
                .claim("Status", user.getStatus())
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaimSet.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }
}
