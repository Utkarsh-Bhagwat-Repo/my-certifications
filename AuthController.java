package com.healthtrack360.controller;

import com.healthtrack360.dto.LoginRequest;
import com.healthtrack360.dto.RefreshTokenRequest;
import com.healthtrack360.dto.TokenResponse;
import com.healthtrack360.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshTokens(request.getRefreshToken()));
    }
}
