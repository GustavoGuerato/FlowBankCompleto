package com.flowbank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.flowbank.dto.AuthRequest;
import com.flowbank.dto.AuthResponse;
import com.flowbank.model.User;
import com.flowbank.repository.UserRepository;
import com.flowbank.util.JwtUtil;

import java.math.BigDecimal;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(AuthRequest req) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            return AuthResponse.builder().message("Email já existe").build();
        }
        User user = User.builder()
                .name(req.getEmail().split("@")[0])
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .balance(BigDecimal.ZERO)
                .build();
        userRepo.save(user);
        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.builder().token(token).message("Registrado com sucesso").build();
    }

    public AuthResponse login(AuthRequest req) {
        var userOpt = userRepo.findByEmail(req.getEmail());
        if (userOpt.isEmpty() || !passwordEncoder.matches(req.getPassword(), userOpt.get().getPassword())) {
            return AuthResponse.builder().message("Credenciais inválidas").build();
        }
        String token = jwtUtil.generateToken(req.getEmail());
        return AuthResponse.builder().token(token).message("Login sucesso").build();
    }
}