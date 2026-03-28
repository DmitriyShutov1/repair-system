package com.system.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtDecoder jwtDecoder;

    // ✅ список публичных endpoint'ов
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/crud/users/reset-password",
            "/api/auth/refresh"
    );

    public JwtAuthGlobalFilter(@Value("${security.jwt.public-key}") String publicKey) {

        RSAPublicKey rsaPublicKey = KeyUtils.parsePublicKey(publicKey);
        this.jwtDecoder = NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

    	
    	// ✅ ВСЕГДА пропускаем CORS preflight
        if (CorsUtils.isPreFlightRequest(exchange.getRequest())) {
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        
//        if (method.equalsIgnoreCase("OPTIONS")) { 
//        	return chain.filter(exchange); 
//        }
        
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing Authorization header");
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {
            Jwt jwt = jwtDecoder.decode(token);

            String userId = jwt.getSubject();
            String role = jwt.getClaimAsString("role");
            String branchId = jwt.getClaimAsString("branchId");

            log.info("JWT validated. userId={}, role={}", userId, role);

            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role != null ? role : "")
                    .headers(headers -> {
                        if (branchId != null) {
                            headers.add("X-Branch-Id", branchId);
                        }
                    })
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (JwtValidationException ex) {

            boolean expired = ex.getErrors().stream()
                    .anyMatch(error -> error.getDescription().toLowerCase().contains("expired"));

            if (expired) {
                return tokenExpired(exchange);
            }

            return unauthorized(exchange);
        }
        catch (JwtException ex) {
            return unauthorized(exchange);
        }
    }

    private boolean isPublic(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
    
    private Mono<Void> tokenExpired(ServerWebExchange exchange) {

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"error\":\"TOKEN_EXPIRED\"}";

        var buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // раньше routing
    }
}