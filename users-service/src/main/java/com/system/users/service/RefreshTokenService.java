package com.system.users.service;

import com.system.users.entity.RefreshToken;
import com.system.users.entity.UserAccount;
import com.system.users.repository.RefreshTokenRepository;
import com.system.users.repository.UserAccountRepository;
import com.system.users.security.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int TOKEN_RAW_RANDOM_BYTES = 48;
    private static final int MAX_GENERATION_ATTEMPTS = 5;
    private static final int MIN_SECRET_BYTES = 32;  

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAccountRepository userAccountRepository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    private final byte[] refreshTokenSecretBytes;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserAccountRepository userAccountRepository,
                               JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userAccountRepository = userAccountRepository;
        this.jwtProperties = jwtProperties;

        if (jwtProperties == null) {
            throw new IllegalStateException("JwtProperties must be provided");
        }

        String rawSecret = jwtProperties.getRefreshTokenHmacSecret();
        if (rawSecret == null || rawSecret.isBlank()) {
            throw new IllegalStateException("refreshTokenHmacSecret must be configured (security.jwt.refresh-token-hmac-secret)");
        }

        this.refreshTokenSecretBytes = decodeMaybeBase64(rawSecret);

        if (this.refreshTokenSecretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("refreshTokenHmacSecret is too short; require at least " + MIN_SECRET_BYTES + " bytes");
        }

        if (jwtProperties.getRefreshTokenExpirationMs() <= 0) {
            log.warn("refreshTokenExpirationMs is not positive; tokens may expire immediately. Check configuration.");
        }
    }


    @Transactional
    public RefreshTokenCreateResult createRefreshToken(Long userId,
                                                       String deviceId,
                                                       String userAgent,
                                                       String ipAddress) {
    	
    	if (jwtProperties.getRefreshTokenHmacSecret() == null ||
    	        jwtProperties.getRefreshTokenHmacSecret().length() < 32) {
    	        throw new IllegalStateException("refreshTokenHmacSecret is not configured properly");
    	    }
    	
    	

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        if (user.getStatus() != UserAccount.Status.ACTIVE) {
            throw new IllegalStateException("User is not active");
        }
        
        int revokedCount = refreshTokenRepository.revokeAllActiveTokensForDevice(user, deviceId);
        log.debug("Revoked {} previous refresh tokens for user={} device={}", revokedCount, userId, deviceId);

        // TTL: use Duration.ofMillis for precise milliseconds handling
        LocalDateTime expiresAt = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMs()));

        for (int attempt = 1; attempt <= MAX_GENERATION_ATTEMPTS; attempt++) {
            String tokenRaw = generateRawToken();
            String tokenHash = hmacSha256Hex(tokenRaw);

            RefreshToken refreshToken = RefreshToken.builder()
                    .tokenHash(tokenHash)
                    .user(user)
                    .deviceId(deviceId)
                    .userAgent(userAgent)
                    .ipAddress(ipAddress)
                    .expiresAt(expiresAt)
                    .revoked(false)
                    .build();

            try {
                refreshTokenRepository.save(refreshToken);
                // Successful creation — return raw token (show it to client once)
                return new RefreshTokenCreateResult(tokenRaw, expiresAt);
            } catch (DataIntegrityViolationException ex) {
                // Possible hash collision or unique constraint violation — retry
                log.warn("Refresh token hash collision or unique constraint violation (attempt {}/{}) — regenerating", attempt, MAX_GENERATION_ATTEMPTS);
                // try again
            }
        }

        throw new RuntimeException("Failed to create unique refresh token after " + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValidByRawToken(String tokenRaw) {
        if (tokenRaw == null || tokenRaw.isBlank()) return Optional.empty();

        String hash = hmacSha256Hex(tokenRaw);
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(hash);

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        RefreshToken token = tokenOpt.get();

        // Check revoked
        if (token.isRevoked()) {
            log.warn("REUSE DETECTED for userId={}", safeGetUserId(token));
            return Optional.empty();
        }

        // Check expiry
        if (token.isExpired()) {
            return Optional.empty();
        }

        // Check user status (blocked users cannot refresh)
        UserAccount user = token.getUser();
        if (user == null) {
            log.warn("Refresh token references missing user (tokenId={})", token.getId());
            return Optional.empty();
        }
        if (user.getStatus() == UserAccount.Status.BLOCKED) {
            log.warn("Refresh token for blocked userId={}", user.getId());
            return Optional.empty();
        }

        return Optional.of(token);
    }

    @Transactional
    public RefreshTokenCreateResult rotateRefreshToken(String oldTokenRaw,
                                                       String deviceId,
                                                       String userAgent,
                                                       String ipAddress) {

        if (oldTokenRaw == null || oldTokenRaw.isBlank()) {
            throw new IllegalArgumentException("oldTokenRaw is required");
        }

        String oldHash = hmacSha256Hex(oldTokenRaw);

        RefreshToken oldToken = refreshTokenRepository.findByTokenHash(oldHash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        // If token already revoked -> reuse attempt
        if (oldToken.isRevoked()) {
            Long uid = safeGetUserId(oldToken);
            log.error("TOKEN REUSE ATTEMPT detected for userId={}", uid);

            // Ensure revocation of all tokens is committed even if we subsequently throw
            userAccountRepository.findById(uid).ifPresent(user -> {
                List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(user);
                if (tokens.isEmpty()) return;
                tokens.forEach(rt -> rt.setRevoked(true));
                refreshTokenRepository.saveAll(tokens);
                log.info("Revoked {} refresh tokens for userId={}", tokens.size(), uid);
            });
            // Throw to signal client that reuse occurred (client should force re-login)
            throw new IllegalStateException("Refresh token reuse detected");
        }

        if (oldToken.isExpired()) {
            throw new IllegalStateException("Refresh token expired");
        }

        // Normal rotation: mark old token revoked and persist, then create a new one
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        return createRefreshToken(
                oldToken.getUser().getId(),
                deviceId,
                userAgent,
                ipAddress
        );
    }

    /**
     * Revoke all tokens for a user and commit immediately in a separate transaction so
     * that revocation is not rolled back if caller throws an exception afterwards.
     */
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void revokeAllForUser(Long userId) {
//        userAccountRepository.findById(userId).ifPresent(user -> {
//            List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(user);
//            if (tokens.isEmpty()) return;
//            tokens.forEach(rt -> rt.setRevoked(true));
//            refreshTokenRepository.saveAll(tokens);
//            log.info("Revoked {} refresh tokens for userId={}", tokens.size(), userId);
//        });
//    }

    @Transactional
    public void removeExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }
    
    
    @Transactional
    public void logoutThisRefresh(UserAccount user, String deficeId) {
    	String device = deficeId;
		refreshTokenRepository.revokeAllActiveTokensForDevice(user, device);
    }
    
    
    @Transactional
    public boolean revokeByRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return false;
        String hash = hmacSha256Hex(rawToken);
        Optional<RefreshToken> opt = refreshTokenRepository.findByTokenHash(hash);
        if (opt.isEmpty()) return false;
        RefreshToken token = opt.get();
        if (token.isRevoked()) return false; // уже revoked
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.info("RefreshTokenService: revoked token id={} for userId={}", token.getId(), token.getUser() != null ? token.getUser().getId() : null);
        return true;
    }


    private String hmacSha256Hex(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(refreshTokenSecretBytes, HMAC_ALGORITHM);
            mac.init(keySpec);

            byte[] rawHmac = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("HMAC calculation failed", e);
        }
    }

    private String generateRawToken() {
        byte[] random = new byte[TOKEN_RAW_RANDOM_BYTES];
        secureRandom.nextBytes(random);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        return randomPart + "." + UUID.randomUUID();
    }

    private static byte[] decodeMaybeBase64(String s) {
        try {
            return Base64.getDecoder().decode(s);
        } catch (IllegalArgumentException ex) {
            return s.getBytes(StandardCharsets.UTF_8);
        }
    }

    private static Long safeGetUserId(RefreshToken token) {
        try {
            return token.getUser() != null ? token.getUser().getId() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    public static final class RefreshTokenCreateResult {
        private final String rawToken;
        private final LocalDateTime expiresAt;

        public RefreshTokenCreateResult(String rawToken, LocalDateTime expiresAt) {
            this.rawToken = rawToken;
            this.expiresAt = expiresAt;
        }

        public String getRawToken() {
            return rawToken;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }

        public long getExpiresAtMillis() {
            return expiresAt == null ? 0L : expiresAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    }
}
