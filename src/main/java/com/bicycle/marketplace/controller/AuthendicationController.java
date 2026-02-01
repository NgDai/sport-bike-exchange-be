package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.AuthenticationRequest;
import com.bicycle.marketplace.dto.request.IntrospecRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.AuthenticationResponse;
import com.bicycle.marketplace.dto.response.IntrospecResponse;
import com.bicycle.marketplace.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Builder
public class AuthendicationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authendicate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospecResponse> introspect(@RequestBody IntrospecRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospecResponse>builder()
                .result(result)
                .build();
    }
}
