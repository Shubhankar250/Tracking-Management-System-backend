package com.trackingpath.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtParentService {

    private final JwtProperties jwtProperties;

    public JwtParentService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // 🔐 Generate Token with Custom Claims
    public String generateToken(CustomUserPrincipal principal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("parentId", principal.getParentId());
        claims.put("passengerId", principal.getPassengerLoginId());
        
        String role = principal.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("ROLE_PARENT");

        claims.put("role", role);

        return buildToken(claims, principal.getUsername());
    }

    private String buildToken(Map<String, Object> claims, String username) {
        long expirationMillis = jwtProperties.expirationSeconds() * 1000;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey())
                .compact();
    }

    // 🔍 Extract Username
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 🔍 Extract Parent ID
    public Long extractParentId(String token) {
        Object value = extractClaim(token, claims -> claims.get("parentId"));
        return value == null ? null : Long.valueOf(value.toString());
    }

    // 🔍 Generic Claim Extractor
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // 🔍 Extract All Claims (Updated API)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 🔐 Token Validation
    public boolean isTokenValid(String token, CustomUserPrincipal user) {
        final String username = extractUsername(token);
        return username.equals(user.getUsername()) && !isTokenExpired(token);
    }

    // ⏳ Expiry Check
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 🔑 Secure Key (Base64)
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}