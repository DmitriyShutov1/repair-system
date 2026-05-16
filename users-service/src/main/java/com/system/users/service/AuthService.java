package com.system.users.service;

import com.system.users.entity.RefreshToken;
import com.system.users.entity.UserAccount;
import com.system.users.repository.BranchRepository;
import com.system.users.repository.UserAccountRepository;
import com.system.users.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserAccountRepository userAccountRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            BranchRepository branchRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public static record RegisterRequest(
            String email,
            String phone,
            String password
    ) {}

    public static record LoginRequest(
            String login,   
            String password
    ) {}

    public static record AuthResponse(
            String accessToken,
            long accessExpiresAtMillis,
            String refreshToken,         
            long refreshExpiresAtMillis
    ) {}

    @Transactional
    public AuthResponse login(LoginRequest req, String deviceId, String userAgent, String ipAddress) {
    	
    	
    	if (deviceId == null || deviceId.isBlank()) {
    	    throw new BadCredentialsException("DeviceId required");
    	}
    	
        if (req == null || req.login() == null || req.login().isBlank() || req.password() == null) {
            throw new BadCredentialsException("Invalid credentials");
        }

        Optional<UserAccount> opt = userAccountRepository.findByEmail(req.login());
        if (opt.isEmpty()) {
            opt = userAccountRepository.findByPhone(req.login());
        }

        UserAccount user = opt.orElseThrow(() -> new BadCredentialsException("User not found"));

        if (user.getStatus() == UserAccount.Status.BLOCKED) {
            throw new DisabledException("User is blocked");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Bad credentials");
        }

        RefreshTokenService.RefreshTokenCreateResult refreshResult =
                refreshTokenService.createRefreshToken(user.getId(), deviceId, userAgent, ipAddress);

        Long branchId = userAccountRepository.findBranchIdByUserId(user.getId()).orElse(null);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name(), branchId);
        long accessExpiresAt = jwtTokenExpiryMillis(accessToken);

        log.info("AuthService: user logged in id={} login={}", user.getId(), req.login());

        return new AuthResponse(
                accessToken,
                accessExpiresAt,
                refreshResult.getRawToken(),
                refreshResult.getExpiresAtMillis()
        );
    }

    @Transactional
    public AuthResponse refresh(String oldRefreshTokenRaw, String deviceId, String userAgent, String ipAddress) {
    	
    	if (deviceId == null || deviceId.isBlank()) {
    	    throw new BadCredentialsException("DeviceId required");
    	}
    	
        if (oldRefreshTokenRaw == null || oldRefreshTokenRaw.isBlank()) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        Optional<RefreshToken> tokenOpt = refreshTokenService.findValidByRawToken(oldRefreshTokenRaw);
        if (tokenOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        RefreshToken token = tokenOpt.get();
        UserAccount user = token.getUser();
        if (user == null) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        if (user.getStatus() == UserAccount.Status.BLOCKED) {
            throw new DisabledException("User is blocked");
        }
        
        
        if (!Objects.equals(token.getDeviceId(), deviceId)) {
            throw new BadCredentialsException("Device mismatch");
        }

        RefreshTokenService.RefreshTokenCreateResult newRefresh;
        try {
            newRefresh = refreshTokenService.rotateRefreshToken(oldRefreshTokenRaw, deviceId, userAgent, ipAddress);
        } catch (IllegalStateException ex) {
            log.warn("Refresh rotation failed for userId={} : {}", user.getId(), ex.getMessage());
            throw new BadCredentialsException("Invalid refresh token");
        }

        Long branchId = userAccountRepository.findBranchIdByUserId(user.getId()).orElse(null);
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name(), branchId);
        long accessExpiresAt = jwtTokenExpiryMillis(newAccessToken);

        log.info("AuthService: rotated refresh token and issued new access token for userId={}", user.getId());

        return new AuthResponse(
                newAccessToken,
                accessExpiresAt,
                newRefresh.getRawToken(),
                newRefresh.getExpiresAtMillis()
        );
    }

    @Transactional
    public void logoutAllSessions(Long userId, String deviceId) {
    	
    	Optional<UserAccount> opt = userAccountRepository.findById(userId);
    	UserAccount user = opt.orElseThrow(() -> new BadCredentialsException("User not found"));
        refreshTokenService.logoutThisRefresh(user, deviceId);
        log.info("AuthService: revokeAllForUser requested for userId={}", userId);
    }

    @Transactional
    public boolean revokeRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return false;
        boolean revoked = refreshTokenService.revokeByRawToken(rawToken);
        if (revoked) {
            log.info("AuthService: revoked one refresh token");
        } else {
            log.info("AuthService: refresh token not found/already revoked");
        }
        return revoked;
    }

    private long jwtTokenExpiryMillis(String token) {
        if (token == null) return 0L;
        try {
            Date exp = jwtService.parseToken(token).getExpiration();
            return exp != null ? exp.getTime() : 0L;
        } catch (Exception ex) {
            log.debug("Failed to parse token expiry: {}", ex.getMessage());
            return 0L;
        }
    }

    private String normalize(String s) {
        return (s == null) ? null : s.trim().toLowerCase();
    }
}
