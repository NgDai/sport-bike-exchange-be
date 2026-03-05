// File: AuthenticationController.java
package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.AuthenticationRequest;
import com.bicycle.marketplace.dto.request.EmailAuthenticationRequest;
import com.bicycle.marketplace.dto.request.IntrospectRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.AuthenticationResponse;
import com.bicycle.marketplace.dto.response.IntrospectResponse;
import com.bicycle.marketplace.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Builder
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/loginEmail")
    ApiResponse<AuthenticationResponse> authenticateEmail(@RequestBody @Valid EmailAuthenticationRequest request) {
        var result = authenticationService.loginWithEmail(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

//    @PostMapping("/request-magic-link")
//    ApiResponse<String> requestMagicLink(@RequestParam String email) {
//        var result = authenticationService.requestMagicLink(email);
//        return ApiResponse.<String>builder()
//                .result(result)
//                .build();
//    }
//
//    @PostMapping("/verify-magic-link")
//    ApiResponse<AuthenticationResponse> verifyMagicLink(@RequestParam String token) {
//        var result = authenticationService.verifyMagicLink(token);
//        return ApiResponse.<AuthenticationResponse>builder()
//                .result(result)
//                .build();
//    }
}