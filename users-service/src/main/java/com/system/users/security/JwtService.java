package com.system.users.security;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long accessTokenExpirationMs;

    public JwtService(JwtProperties properties) {
        if (properties.getPrivateKey() == null || properties.getPublicKey() == null) {
            throw new IllegalArgumentException("RSA keys must be configured in security.jwt.private-key/public-key");
        }

        this.privateKey = loadPrivateKey(properties.getPrivateKey());
        this.publicKey = loadPublicKey(properties.getPublicKey());
        this.accessTokenExpirationMs = properties.getAccessTokenExpirationMs();

        log.info("JwtService initialized with RS256. Access token ttl (ms) = {}", accessTokenExpirationMs);
    }


    public String generateAccessToken(Long userId, String role, Long branchId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("role", role);

        if (branchId != null) {
            builder.claim("branchId", branchId);
        }

        return builder
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }


    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException ex) {
            log.debug("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    public Long extractUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    public String extractRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public Long extractBranchId(String token) {
        return parseToken(token).get("branchId", Long.class);
    }


    private PrivateKey loadPrivateKey(String pem) {
        try {
            String cleaned = pem
                    .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(cleaned);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);

            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load RSA private key. Must be PKCS#8 format.", ex);
        }
    }

    private PublicKey loadPublicKey(String pem) {
        try {
            String cleaned = pem
                    .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(cleaned);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load RSA public key.", ex);
        }
    }
}