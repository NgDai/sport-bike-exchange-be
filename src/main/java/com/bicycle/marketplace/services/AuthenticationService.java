package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.EmailAuthenticationRequest;
import com.bicycle.marketplace.dto.request.GoogleAuthRequest;
import com.bicycle.marketplace.repository.IUserRepository;
import com.bicycle.marketplace.dto.request.AuthenticationRequest;
import com.bicycle.marketplace.dto.request.IntrospectRequest;
import com.bicycle.marketplace.dto.response.AuthenticationResponse;
import com.bicycle.marketplace.dto.response.IntrospectResponse;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    IUserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signer.key}")
    protected String signerKey;

    @NonFinal
    @Value("${GOOGLE_CLIENT_ID}")
    protected String googleClientId;

    public IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException {
        var token = request.getToken();
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);

        return IntrospectResponse.builder()
                .valid(verified && expirationTime.after(new Date()))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if ("Inactive".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.USER_INACTIVE);
        }

        boolean authenticated = request.getPassword().equals(user.getPassword());
        // boolean authenticated = passwordEncoder.matches(request.getPassword(),
        // user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.USER_INVALID_AUTHENTICATION);
        }

        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public AuthenticationResponse loginWithEmail(EmailAuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if ("Inactive".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.USER_INACTIVE);
        }

        // boolean authenticated = passwordEncoder.matches(request.getPassword(),
        // user.getPassword());
        boolean authenticated = request.getPassword().equals(user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.USER_INVALID_AUTHENTICATION);
        }

        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    private String generateToken(Users user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        JWTClaimsSet jwtClaimSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("BicycleMarketplace")
                .jwtID(UUID.randomUUID().toString())
                .claim("FullName", user.getFullName())
                .claim("scope", buildScope(user))
                .claim("avatar", user.getAvatar())
                .claim("userId", user.getUserId())
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(5, ChronoUnit.HOURS).toEpochMilli()))
                .build();
        JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaimSet.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildScope(Users user) {
        StringJoiner scope = new StringJoiner(" ");
        if (user.getRole() != null && !user.getRole().isEmpty())
            scope.add(user.getRole());
        return scope.toString();
    }

    public AuthenticationResponse loginWithGoogle(GoogleAuthRequest request) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(request.getIdToken());
        if (idToken == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");
        String phone = (String) payload.get("phone_number");
        String googleId = payload.getSubject();

        Users user = userRepository.findByEmail(email).orElseGet(() -> {
            Users newUser = Users.builder()
                    .email(email)
                    .username(email) // dùng email làm username cho Google user
                    .fullName(name)
                    .avatar(picture)
                    .googleId(googleId)
                    .phone(phone)
                    .status("Active")
                    .role("USER")
                    .build();
            Users savedUser = userRepository.save(newUser);
            return savedUser;
        });

        if ("Inactive".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.USER_INACTIVE);
        }

        // Trả về JWT nội bộ như flow đăng nhập thường
        return AuthenticationResponse.builder()
                .token(generateToken(user))
                .build();
    }
}