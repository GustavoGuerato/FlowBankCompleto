package com.flowbank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.flowbank.dto.AuthRequest;
import com.flowbank.dto.AuthResponse;
import com.flowbank.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest req) {
        return authService.login(req);
    }
}