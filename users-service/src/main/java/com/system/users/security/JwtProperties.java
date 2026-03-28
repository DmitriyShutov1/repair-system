package com.system.users.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String privateKey;

    private String publicKey;

    private long accessTokenExpirationMs;

    private long refreshTokenExpirationMs;

    private String refreshTokenHmacSecret;
}