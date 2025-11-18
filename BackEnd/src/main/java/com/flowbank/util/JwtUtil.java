package com.flowbank.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // A chave secreta deve ser longa o suficiente para HS512 (e.g., 64 caracteres
    // em Base64)
    @Value("${jwt.secret:minhaChaveSecretaMuitoLongaParaProducao1234567890}")
    private String secret;

    // Garante que a chave de assinatura seja a mesma toda vez que o serviço rodar
    private SecretKey getSigningKey() {
        // Decodifica a string Base64 do secret para bytes e cria a chave HmacShaKeyFor
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10h de validade
                .signWith(getSigningKey())
                .compact();
    }

    // ⭐ MÉTODO CORRIGIDO: Agora é 'extractUsername'
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject(); // O subject é o email que você colocou no generateToken
    }

    public boolean validateToken(String token, String email) {
        // ⭐ USANDO: extractUsername
        return (email.equals(extractUsername(token)) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }
}