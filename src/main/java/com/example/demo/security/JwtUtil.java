package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration-millis}")
    private long expirationMillis;

    /**
     * 根据配置文件中的 secret 生成签名密钥
     */
    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(username)       // 主题，这里存用户名
                .issuedAt(now)          // 签发时间
                .expiration(expireDate) // 过期时间
                .signWith(getSignKey()) // 签名
                .compact();
    }

    /**
     * 解析全部 Claims
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 token 中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * 校验 token 是否过期
     */
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * 校验 token 是否有效（用户名匹配且未过期）
     */
    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}
