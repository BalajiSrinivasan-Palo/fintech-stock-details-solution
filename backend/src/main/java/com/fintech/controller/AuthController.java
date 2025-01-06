package com.fintech.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Base64;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Demo: Accept any username/password
        String token = Jwts.builder()
            .setSubject(loginRequest.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 864000000)) // 24 hours
            .signWith(SignatureAlgorithm.HS256, Base64.getDecoder().decode(jwtSecret))
            .compact();

        return ResponseEntity.ok(new JwtResponse(token));
    }
}

@Data
class LoginRequest {
    private String username;
    private String password;
}

@Data
class JwtResponse {
    private final String token;
} 