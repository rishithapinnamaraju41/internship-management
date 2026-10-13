// src/main/java/com/appticode/security/JwtTokenProvider.java
package com.appticode.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private int jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private int refreshExpiration;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "access")
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
        
        logger.info("🔑 Access token generated for: {}", userDetails.getUsername());
        return token;
    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "refresh")
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
        
        logger.info("🔄 Refresh token generated for: {}", userDetails.getUsername());
        return token;
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String username = claims.getSubject();
            logger.debug("👤 Extracted username: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("❌ Failed to extract username: {}", e.getMessage());
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token);
            logger.debug("✅ Token validated successfully");
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("❌ JWT token is expired: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("❌ Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token);
        if (username == null) {
            throw new RuntimeException("Invalid token");
        }
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public Map<String, Object> getTokenDetails(String token) {
        Map<String, Object> details = new HashMap<>();
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            details.put("username", claims.getSubject());
            details.put("issuedAt", claims.getIssuedAt());
            details.put("expiration", claims.getExpiration());
            details.put("isExpired", claims.getExpiration().before(new Date()));
            details.put("type", claims.get("type"));
            return details;
        } catch (ExpiredJwtException e) {
            details.put("error", "Token expired");
            details.put("isExpired", true);
            return details;
        } catch (Exception e) {
            details.put("error", e.getMessage());
            return details;
        }
    }
}