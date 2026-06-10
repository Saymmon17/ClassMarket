package com.classmarket.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${classmarket.jwt.secret}") String secret,
            @Value("${classmarket.jwt.expiration-ms}") long expirationMs) {
        this.key          = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secret));
        this.expirationMs = expirationMs;
    }

    public String gerarToken(String email, boolean adm) {
        return Jwts.builder()
                .setSubject(email)
                .claim("adm", adm)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isAdm(String token) {
        return Boolean.TRUE.equals(getClaims(token).get("adm", Boolean.class));
    }

    public boolean validar(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
