package com.system.users.controller;

import com.system.users.service.AuthService;
import com.system.users.service.RefreshTokenService;
import com.system.users.security.JwtService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    public record PublicAuthResponse(
            String accessToken,
            long accessExpiresAtMillis
    ) {}
    
    public static record LoginDto(
            @NotBlank(message = "login required") String login,
            @NotBlank(message = "password required") String password
    ) {}

    public static record RefreshDto(
            @NotBlank(message = "refreshToken required") String refreshToken
    ) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto dto, HttpServletRequest request, HttpServletResponse response) {
    	try {
            String deviceId = request.getHeader("X-Device-Id");
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getRemoteAddr();

            var serviceResp = authService.login(new AuthService.LoginRequest(dto.login(), dto.password()), deviceId, userAgent, ip);
            
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", serviceResp.refreshToken())
                    .httpOnly(true)
                    .secure(false) // В DEV можно false
                    .path("/api/auth")
                    .maxAge(Duration.ofDays(30))
                    .sameSite("Lax") // или Lax
                    .build();
            
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            
            return ResponseEntity.ok(
                    new PublicAuthResponse(
                            serviceResp.accessToken(),
                            serviceResp.accessExpiresAtMillis()
                    )
            );
    	} catch (BadCredentialsException e) {
            // Неправильный логин/пароль или пользователь не найден
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "error", "Неверный логин или пароль",
                        "message", e.getMessage()
                    ));
                    
        } catch (DisabledException e) {
            // Пользователь заблокирован
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "error", "Аккаунт заблокирован",
                        "message", e.getMessage()
                    ));
                    
        } catch (Exception e) {
            // Любая другая неожиданная ошибка
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Внутренняя ошибка сервера",
                        "message", e.getMessage()
                    ));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
    	
	    	String refreshToken = null;
	
	        if (request.getCookies() != null) {
	            for (Cookie cookie : request.getCookies()) {
	                if ("refreshToken".equals(cookie.getName())) {
	                    refreshToken = cookie.getValue();
	                    break;
	                }
	            }
	        }
	        
	        if (refreshToken == null) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	        }
    	
            String deviceId = request.getHeader("X-Device-Id");
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getRemoteAddr();
           
            
            var serviceResp = authService.refresh(refreshToken, deviceId, userAgent, ip);
            
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", serviceResp.refreshToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/api/auth")
                    .maxAge(Duration.ofMillis(serviceResp.refreshExpiresAtMillis() - System.currentTimeMillis()))
                    .sameSite("Lax")
                    .build();
            
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            
            
            return ResponseEntity.ok(
                    new PublicAuthResponse(
                            serviceResp.accessToken(),
                            serviceResp.accessExpiresAtMillis()
                    )
            );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response,
    		@RequestHeader(value = "X-User-Id", required = false) String userIdHeader, 
    		@RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
    	
            Long userId = Long.parseLong(userIdHeader);
            authService.logoutAllSessions(userId, deviceId);
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/api/auth")
                    .maxAge(0)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
            return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/test/hello")
    public String helloFromUsers() {
        return "Привет из users сервиса!";
    }
}
