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

/**
 * AuthService — регистрация, логин и refresh flow.
 *
 * - constructor injection
 * - register/login/refresh возвращают access + refresh tokens
 * - refresh flow использует RefreshTokenService (rotation + reuse detection)
 *
 * Примечания:
 * - RefreshTokenService обеспечивает безопасное хранение и rotation refresh-токенов.
 * - AuthService валидирует статус пользователя и выдаёт access token через JwtService.
 */
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

    /* -------------------------
       DTOs (records)
       ------------------------- */
    public static record RegisterRequest(
            String email,
            String phone,
            String password
    ) {}

    public static record LoginRequest(
            String login,   // email or phone
            String password
    ) {}

    /**
     * Response contains access token (+expiry) and refresh token raw value (+expiry).
     * Refresh raw token MUST be stored by client securely; server stores only hash.
     */
    public static record AuthResponse(
            String accessToken,
            long accessExpiresAtMillis,
            String refreshToken,         // raw token (only shown once)
            long refreshExpiresAtMillis
    ) {}

    /* -------------------------
       Register
       ------------------------- */
//    @Transactional
//    public AuthResponse register(RegisterRequest req, String deviceId, String userAgent, String ipAddress) {
//    	
//    	if (deviceId == null || deviceId.isBlank()) {
//    	    deviceId = "default"; // fallback для тестов/Postman
//    	}
//    	
//        if (req == null) throw new IllegalArgumentException("Register request is null");
//
//        // require at least one identifier
//        if ((req.email() == null || req.email().isBlank()) && (req.phone() == null || req.phone().isBlank())) {
//            throw new IllegalArgumentException("email or phone is required");
//        }
//
//        if (req.password() == null || req.password().length() < 6) {
//            throw new IllegalArgumentException("password must be at least 6 characters");
//        }
//        
//        //добавил нормализацию при проверке
//
//        if (req.email() != null && !req.email().isBlank() && userAccountRepository.existsByEmail(normalize(req.email()))) {
//            throw new IllegalArgumentException("email already in use");
//        }
//        if (req.phone() != null && !req.phone().isBlank() && userAccountRepository.existsByPhone(normalize(req.phone()))) {
//            throw new IllegalArgumentException("phone already in use");
//        }
//
//        // resolve role (default CLIENT)
//        UserAccount.Role role = UserAccount.Role.CLIENT;
//
//        // resolve branch if provided (left null for now)
//        // Branch branch = ...; (if required)
//        // we'll keep branch null for new registrations
//        // build and save user
//        UserAccount user = UserAccount.builder()
//                .email(normalize(req.email()))
//                .phone(normalize(req.phone()))
//                .passwordHash(passwordEncoder.encode(req.password()))
//                .role(role)
//                .status(UserAccount.Status.ACTIVE)
//                .branch(null)
//                .build();
//
//        user = userAccountRepository.save(user);
//
//        log.info("AuthService: registered user id={} email={} phone={}", user.getId(), user.getEmail(), user.getPhone());
//
//        // create refresh token (stored hashed in DB) — returns raw token to client
//        RefreshTokenService.RefreshTokenCreateResult refreshResult =
//                refreshTokenService.createRefreshToken(user.getId(), deviceId, userAgent, ipAddress);
//
//        // generate access token
//        Long branchIdForToken = null; // branch is null on register
//        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name(), branchIdForToken);
//        long accessExpiresAt = jwtTokenExpiryMillis(accessToken);
//
//        return new AuthResponse(
//                accessToken,
//                accessExpiresAt,
//                refreshResult.getRawToken(),
//                refreshResult.getExpiresAtMillis()
//        );
//    }

    /* -------------------------
       Login
       ------------------------- */
    @Transactional
    public AuthResponse login(LoginRequest req, String deviceId, String userAgent, String ipAddress) {
    	
    	
    	//ПОМЕНЯЛ, БЫЛО - ДЭФОЛТ, НО ТАК НЕОЧЕНЬ
    	if (deviceId == null || deviceId.isBlank()) {
    		//deviceId = "default";
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

        // create refresh token for this session/device
        RefreshTokenService.RefreshTokenCreateResult refreshResult =
                refreshTokenService.createRefreshToken(user.getId(), deviceId, userAgent, ipAddress);

        // obtain branchId safely (repository method returns Optional<Long>)
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

    /* -------------------------
       Refresh (rotate + new access token)
       ------------------------- */
    @Transactional
    public AuthResponse refresh(String oldRefreshTokenRaw, String deviceId, String userAgent, String ipAddress) {
    	
    	//ПОМЕНЯЛ, БЫЛО - ДЭФОЛТ, НО ТАК НЕОЧЕНЬ
    	if (deviceId == null || deviceId.isBlank()) {
    		//deviceId = "default";
    	    throw new BadCredentialsException("DeviceId required");
    	}
    	
        if (oldRefreshTokenRaw == null || oldRefreshTokenRaw.isBlank()) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        // First, check token validity (existence, not revoked, not expired, user status)
        Optional<RefreshToken> tokenOpt = refreshTokenService.findValidByRawToken(oldRefreshTokenRaw);
        if (tokenOpt.isEmpty()) {
            // invalid token or reuse/expired/blocked
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
        
        
        //ДОБАВИЛ ДЛЯ ИЗБЕЖАНИЯ КРАЖ
        if (!Objects.equals(token.getDeviceId(), deviceId)) {
            throw new BadCredentialsException("Device mismatch");
        }

        // Rotate: this will revoke the old one and create+persist a new refresh token (with reuse detection inside)
        RefreshTokenService.RefreshTokenCreateResult newRefresh;
        try {
            newRefresh = refreshTokenService.rotateRefreshToken(oldRefreshTokenRaw, deviceId, userAgent, ipAddress);
        } catch (IllegalStateException ex) {
            // rotation detected reuse or other critical condition; propagate as auth failure
            log.warn("Refresh rotation failed for userId={} : {}", user.getId(), ex.getMessage());
            throw new BadCredentialsException("Invalid refresh token");
        }

        // Generate new access token
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

    /* -------------------------
       Logout / revoke
       ------------------------- */
    @Transactional
    public void logoutAllSessions(Long userId, String deviceId) {
        // revoke all refresh tokens for user (REQUIRES_NEW ensures commit in RefreshTokenService)
    	
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

    /* -------------------------
       Helper
       ------------------------- */

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
