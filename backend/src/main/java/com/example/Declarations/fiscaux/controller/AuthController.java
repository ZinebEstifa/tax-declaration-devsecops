package com.example.Declarations.fiscaux.controller;

import com.example.Declarations.fiscaux.dto.LoginRequest;
import com.example.Declarations.fiscaux.dto.LoginResponse;
import com.example.Declarations.fiscaux.dto.RegisterRequest;
import com.example.Declarations.fiscaux.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            if (response.getError() != null && response.getError().contains("bloqué")) {
                return ResponseEntity.status(403).body(response);
            }
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        LoginResponse response = authService.register(registerRequest);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
