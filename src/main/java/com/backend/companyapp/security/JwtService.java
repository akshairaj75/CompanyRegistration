package com.backend.companyapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.backend.companyapp.entity.User;

import java.security.Key;
import java.util.Date;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    // public String generateToken(Authentication authentication, String role) {
    //     String username = authentication.getName();
    //     return generateToken(username, role);
    // }

    // public String generateToken(String username, String role) {
    //     Date now = new Date();
    //     Date expiryDate = new Date(now.getTime() + jwtExpiration);

    //     return Jwts.builder()
    //             .setSubject(username)
    //             .claim("role", role)
    //             .setIssuedAt(now)
    //             .setExpiration(expiryDate)
    //             .signWith(getSigningKey())
    //             .compact();
    // }


    public String generateToken(User user) {

    return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("role", user.getRole())
            .claim("userId", user.getId())
            .claim("fullName", user.getFullName())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSigningKey())
            .compact();
}


    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

        public boolean validateToken(String token, UserDetails userDetails) {
        return getUsernameFromToken(token)
                .equals(userDetails
                        .getUsername())
                && !isTokenExpired(token);

    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }

    public long getExpirationDuration() {
        return jwtExpiration;
    }
}
