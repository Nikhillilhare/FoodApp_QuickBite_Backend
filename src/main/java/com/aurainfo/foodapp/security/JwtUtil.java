package com.aurainfo.foodapp.security;

import com.aurainfo.foodapp.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ROLE_CLAIM = "role";
    private static final String ACCESS_TOKEN = "ACCESS";
    private static final String REFRESH_TOKEN = "REFRESH";

    private final SecretKey signingKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiry-ms}") long accessTokenExpiryMs,
            @Value("${app.jwt.refresh-token-expiry-ms}") long refreshTokenExpiryMs
    )
    //JWT Secret
    {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must be configured");
        }
        byte[] secretBytes =
                secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes"
            );
        }

        if (accessTokenExpiryMs <= 0) {
            throw new IllegalArgumentException(
                    "Access token expiry must be greater than zero"
            );
        }

        if (refreshTokenExpiryMs <= 0) {
            throw new IllegalArgumentException(
                    "Refresh token expiry must be greater than zero"
            );


        }

//        //HS256 Key part code
//        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
//            throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
//        }
//        if (accessTokenExpiryMs <= 0 || refreshTokenExpiryMs <= 0) {
//            throw new IllegalArgumentException("JWT expiry values must be greater than zero");
//        }
        this.signingKey =
                Keys.hmacShaKeyFor(secretBytes);

        this.accessTokenExpiryMs =
                accessTokenExpiryMs;

        this.refreshTokenExpiryMs =
                refreshTokenExpiryMs;

    }

    // =====================================================
    // ACCESS TOKEN
    // =====================================================

    public String generateAccessToken(User user) {
        return generateToken(user, ACCESS_TOKEN, accessTokenExpiryMs);
    }

    // =====================================================
    // REFRESH TOKEN
    // =====================================================
    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESH_TOKEN, refreshTokenExpiryMs);
    }

    // =====================================================
    // CLAIM EXTRACTION
    // =====================================================
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractTokenType(String token) {
        return parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    public String extractRole(String token) {
        return parseClaims(token).get(ROLE_CLAIM, String.class);
    }

    // =====================================================
    // TOKEN VALIDATION
    // =====================================================

    public boolean validateAccessToken(String token) {
        return isValidToken(token, ACCESS_TOKEN);
    }

    public boolean validateRefreshToken(String token) {
        return isValidToken(token, REFRESH_TOKEN);
    }
    // =====================================================
    // TOKEN GENERATION
    // =====================================================

    private String generateToken(User user, String tokenType, long expiryMs) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expiryMs);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .claim(ROLE_CLAIM, user.getRole().name())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    // =====================================================
    // VALIDATION INTERNAL
    // =====================================================

    private boolean isValidToken(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            return expectedType.equals(
                    claims.get(TOKEN_TYPE_CLAIM, String.class)
            );
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }


    // =====================================================
    // PARSE CLAIMS
    // =====================================================

    private Claims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT token cannot be empty");
        }

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
